package com.medisalud.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisalud.domain.port.CalendarioFestivosPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CitaControllerIntegrationTest {

    private static final UUID MEDICO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTRO_MEDICO_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID TERCER_MEDICO_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CalendarioFestivosPort calendarioFestivos;

    @Test
    void reservaConflictoCancelacionYLiberacionDeFranja() throws Exception {
        UUID pacienteId = registrarPaciente();
        OffsetDateTime fechaHora = siguienteDiaLaboral().toOffsetDateTime();
        String reserva = objectMapper.writeValueAsString(Map.of(
                "pacienteId", pacienteId,
                "medicoId", MEDICO_ID,
                "fechaHora", fechaHora));

        String primeraRespuesta = mockMvc.perform(post("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reserva))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andReturn().getResponse().getContentAsString();
        UUID citaId = UUID.fromString(objectMapper.readTree(primeraRespuesta).get("id").asText());

        String disponibilidad = mockMvc.perform(get("/api/v1/medicos/{id}/disponibilidad", MEDICO_ID)
                        .param("fechaInicio", fechaHora.toLocalDate().toString())
                        .param("fechaFin", fechaHora.toLocalDate().toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode resumenDisponibilidad = objectMapper.readTree(disponibilidad);
        JsonNode franjasDisponibles = resumenDisponibilidad.get("franjasDisponibles");
        assertThat(resumenDisponibilidad.get("cantidadFranjasDisponibles").asInt())
                .isEqualTo(franjasDisponibles.size());
        assertThat(franjasDisponibles)
                .noneMatch(franja -> franja.get("inicio").asText().equals(fechaHora.toString()));

        mockMvc.perform(post("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reserva))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FRANJA_MEDICO_OCUPADA"));

        mockMvc.perform(patch("/api/v1/citas/{id}/cancelacion", citaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        mockMvc.perform(post("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reserva))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    @Test
    void debeRevertirLaCancelacionSiLaReprogramacionFalla() throws Exception {
        UUID primerPaciente = registrarPaciente();
        UUID segundoPaciente = registrarPaciente();
        OffsetDateTime primeraFranja = siguienteDiaLaboral().toOffsetDateTime();
        OffsetDateTime franjaOcupada = primeraFranja.plusMinutes(30);

        UUID primeraCitaId = reservar(primerPaciente, OTRO_MEDICO_ID, primeraFranja);
        reservar(segundoPaciente, OTRO_MEDICO_ID, franjaOcupada);

        String reprogramacion = objectMapper.writeValueAsString(Map.of("nuevaFechaHora", franjaOcupada));
        mockMvc.perform(post("/api/v1/citas/{id}/reprogramacion", primeraCitaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reprogramacion))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FRANJA_MEDICO_OCUPADA"));

        mockMvc.perform(get("/api/v1/citas").param("pacienteId", primerPaciente.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].id").value(primeraCitaId.toString()))
                .andExpect(jsonPath("$.contenido[0].estado").value("PROGRAMADA"));
    }

    @Test
    void pacienteNoPuedeReservarLaMismaFranjaConOtroMedico() throws Exception {
        UUID pacienteId = registrarPaciente();
        OffsetDateTime fechaHora = siguienteDiaLaboral(10).toOffsetDateTime();
        reservar(pacienteId, MEDICO_ID, fechaHora);
        String segundaReserva = objectMapper.writeValueAsString(Map.of(
                "pacienteId", pacienteId,
                "medicoId", OTRO_MEDICO_ID,
                "fechaHora", fechaHora));

        mockMvc.perform(post("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(segundaReserva))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FRANJA_PACIENTE_OCUPADA"));
    }

    @Test
    void debeReprogramarYEncontrarCadaCitaConFiltrosCombinados() throws Exception {
        UUID pacienteId = registrarPaciente();
        OffsetDateTime fechaOriginal = siguienteDiaLaboral(20).toOffsetDateTime();
        OffsetDateTime nuevaFecha = fechaOriginal.plusMinutes(30);
        UUID citaOriginalId = reservar(pacienteId, TERCER_MEDICO_ID, fechaOriginal);

        String request = objectMapper.writeValueAsString(Map.of("nuevaFechaHora", nuevaFecha));
        String response = mockMvc.perform(post("/api/v1/citas/{id}/reprogramacion", citaOriginalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.pacienteId").value(pacienteId.toString()))
                .andExpect(jsonPath("$.medicoId").value(TERCER_MEDICO_ID.toString()))
                .andReturn().getResponse().getContentAsString();
        JsonNode nuevaCita = objectMapper.readTree(response);
        UUID nuevaCitaId = UUID.fromString(nuevaCita.get("id").asText());
        assertThat(nuevaCitaId).isNotEqualTo(citaOriginalId);
        assertThat(OffsetDateTime.parse(nuevaCita.get("fechaHora").asText()).toInstant())
                .isEqualTo(nuevaFecha.toInstant());

        mockMvc.perform(get("/api/v1/citas")
                        .param("medicoId", TERCER_MEDICO_ID.toString())
                        .param("pacienteId", pacienteId.toString())
                        .param("estado", "PROGRAMADA")
                        .param("fechaInicio", nuevaFecha.minusMinutes(1).toString())
                        .param("fechaFin", nuevaFecha.plusMinutes(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(nuevaCitaId.toString()))
                .andExpect(jsonPath("$.contenido[0].estado").value("PROGRAMADA"));

        mockMvc.perform(get("/api/v1/citas")
                        .param("medicoId", TERCER_MEDICO_ID.toString())
                        .param("pacienteId", pacienteId.toString())
                        .param("estado", "CANCELADA")
                        .param("fechaInicio", fechaOriginal.toString())
                        .param("fechaFin", fechaOriginal.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(citaOriginalId.toString()))
                .andExpect(jsonPath("$.contenido[0].estado").value("CANCELADA"));
    }

    @Test
    void consultarDomingoDebeRetornarResumenSinFranjas() throws Exception {
        LocalDate domingo = LocalDate.now(ZONA).plusDays(1);
        while (domingo.getDayOfWeek() != DayOfWeek.SUNDAY) {
            domingo = domingo.plusDays(1);
        }

        mockMvc.perform(get("/api/v1/medicos/{id}/disponibilidad", MEDICO_ID)
                        .param("fechaInicio", domingo.toString())
                        .param("fechaFin", domingo.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadFranjasDisponibles").value(0))
                .andExpect(jsonPath("$.franjasDisponibles").isArray())
                .andExpect(jsonPath("$.franjasDisponibles").isEmpty());
    }

    @Test
    void debePaginarCitasConOrdenEstableYMetadatos() throws Exception {
        UUID pacienteId = registrarPaciente();
        OffsetDateTime primeraFranja = siguienteDiaLaboral(40).toOffsetDateTime();
        UUID primeraCita = reservar(pacienteId, MEDICO_ID, primeraFranja);
        UUID segundaCita = reservar(pacienteId, MEDICO_ID, primeraFranja.plusMinutes(30));

        mockMvc.perform(get("/api/v1/citas")
                        .param("pacienteId", pacienteId.toString())
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(primeraCita.toString()))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanio").value(1))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.primera").value(true))
                .andExpect(jsonPath("$.ultima").value(false));

        mockMvc.perform(get("/api/v1/citas")
                        .param("pacienteId", pacienteId.toString())
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].id").value(segundaCita.toString()))
                .andExpect(jsonPath("$.primera").value(false))
                .andExpect(jsonPath("$.ultima").value(true));
    }

    @Test
    void debeRechazarLimitesInvalidosDePaginacion() throws Exception {
        mockMvc.perform(get("/api/v1/citas").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PAGINA_INVALIDA"));

        mockMvc.perform(get("/api/v1/citas").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("TAMANIO_PAGINA_INVALIDO"));
    }

    @Test
    void debeRechazarDisponibilidadMayorANoventaDias() throws Exception {
        LocalDate inicio = LocalDate.now(ZONA).plusDays(1);

        mockMvc.perform(get("/api/v1/medicos/{id}/disponibilidad", MEDICO_ID)
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", inicio.plusDays(90).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("RANGO_DEMASIADO_AMPLIO"));
    }

    private UUID reservar(UUID pacienteId, UUID medicoId, OffsetDateTime fechaHora) throws Exception {
        String request = objectMapper.writeValueAsString(Map.of(
                "pacienteId", pacienteId,
                "medicoId", medicoId,
                "fechaHora", fechaHora));
        String response = mockMvc.perform(post("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private UUID registrarPaciente() throws Exception {
        String documento = "DOC" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String request = objectMapper.writeValueAsString(Map.of(
                "nombreCompleto", "Paciente Integracion",
                "documentoIdentidad", documento,
                "telefono", "3001234567",
                "email", documento.toLowerCase() + "@example.com"));
        String response = mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return UUID.fromString(json.get("id").asText());
    }

    private ZonedDateTime siguienteDiaLaboral() {
        return siguienteDiaLaboral(2);
    }

    private ZonedDateTime siguienteDiaLaboral(int diasDesdeHoy) {
        ZonedDateTime fecha = ZonedDateTime.now(ZONA).plusDays(diasDesdeHoy)
                .withHour(8).withMinute(0).withSecond(0).withNano(0);
        while (fecha.getDayOfWeek() == DayOfWeek.SATURDAY
                || fecha.getDayOfWeek() == DayOfWeek.SUNDAY
                || calendarioFestivos.esFestivo(fecha.toLocalDate())) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }
}
