package com.medisalud.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local", "rn05"})
class PenalizacionRn05IntegrationTest {

    private static final UUID MEDICO_PENALIZACIONES =
            UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID MEDICO_RESERVA_FINAL =
            UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final OffsetDateTime CITA_TARDIA =
            OffsetDateTime.parse("2030-01-16T11:30:00-05:00");
    private static final OffsetDateTime CITA_POSTERIOR =
            OffsetDateTime.parse("2030-01-16T12:00:00-05:00");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Clock reloj;

    @Test
    void debeBloquearNuevasReservasDespuesDeTresCancelacionesTardias() throws Exception {
        assertThat(reloj.instant()).isEqualTo(Instant.parse("2030-01-16T15:20:00Z"));
        UUID pacienteId = registrarPaciente();

        for (int numero = 1; numero <= 3; numero++) {
            UUID citaId = reservar(pacienteId, MEDICO_PENALIZACIONES, CITA_TARDIA);
            mockMvc.perform(patch("/api/v1/citas/{id}/cancelacion", citaId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("CANCELADA"));
        }

        String nuevaReserva = crearReserva(pacienteId, MEDICO_RESERVA_FINAL, CITA_POSTERIOR);
        mockMvc.perform(post("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevaReserva))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PACIENTE_BLOQUEADO"));
    }

    private UUID registrarPaciente() throws Exception {
        String documento = "RN05" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String request = objectMapper.writeValueAsString(Map.of(
                "nombreCompleto", "Paciente Penalizaciones",
                "documentoIdentidad", documento,
                "telefono", "3001234567",
                "email", documento.toLowerCase() + "@example.com",
                "fechaNacimiento", "1992-04-15"));
        String response = mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        return UUID.fromString(body.get("id").asText());
    }

    private UUID reservar(UUID pacienteId, UUID medicoId, OffsetDateTime fechaHora) throws Exception {
        String response = mockMvc.perform(post("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearReserva(pacienteId, medicoId, fechaHora)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private String crearReserva(UUID pacienteId, UUID medicoId, OffsetDateTime fechaHora) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "pacienteId", pacienteId,
                "medicoId", medicoId,
                "fechaHora", fechaHora));
    }
}
