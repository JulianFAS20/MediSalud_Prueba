package com.medisalud.application.usecase;

import com.medisalud.domain.model.Medico;
import com.medisalud.domain.exception.NotFoundException;
import com.medisalud.domain.exception.ValidationException;
import com.medisalud.domain.port.CalendarioFestivosPort;
import com.medisalud.domain.port.CitaRepositoryPort;
import com.medisalud.domain.port.MedicoRepositoryPort;
import com.medisalud.domain.service.PoliticaHorarioAtencion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsultarDisponibilidadUseCaseTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Instant AHORA = Instant.parse("2026-06-10T12:00:00Z");

    @Mock
    private MedicoRepositoryPort medicos;
    @Mock
    private CitaRepositoryPort citas;
    @Mock
    private CalendarioFestivosPort festivos;

    private UUID medicoId;
    private ConsultarDisponibilidadUseCase useCase;

    @BeforeEach
    void configurar() {
        Medico medico = Medico.registrar("Dra. Ana Lopez", "Dermatologia", "5551003", "ana@medisalud.com");
        medicoId = medico.id();
        lenient().when(medicos.buscarPorId(medicoId)).thenReturn(Optional.of(medico));
        useCase = new ConsultarDisponibilidadUseCase(
                medicos, citas, new PoliticaHorarioAtencion(festivos),
                Clock.fixed(AHORA, ZoneOffset.UTC), ZONA, 90);
    }

    @Test
    void debeGenerarVeinteFranjasEnSemanaYExcluirLaOcupada() {
        LocalDate jueves = LocalDate.of(2026, 6, 11);
        Instant ocupada = jueves.atTime(8, 30).atZone(ZONA).toInstant();
        when(citas.buscarFranjasOcupadas(eq(medicoId), any(Instant.class), any(Instant.class)))
                .thenReturn(Set.of(ocupada));

        var resultado = useCase.ejecutar(medicoId, jueves, jueves);

        assertThat(resultado).hasSize(19);
        assertThat(resultado).noneMatch(franja -> franja.inicio().equals(ocupada));
        assertThat(resultado.getFirst().inicio()).isEqualTo(jueves.atTime(8, 0).atZone(ZONA).toInstant());
        assertThat(resultado.getLast().fin()).isEqualTo(jueves.atTime(18, 0).atZone(ZONA).toInstant());
    }

    @Test
    void debeGenerarDiezFranjasElSabado() {
        LocalDate sabado = LocalDate.of(2026, 6, 13);
        when(citas.buscarFranjasOcupadas(eq(medicoId), any(Instant.class), any(Instant.class)))
                .thenReturn(Set.of());

        var resultado = useCase.ejecutar(medicoId, sabado, sabado);

        assertThat(resultado).hasSize(10);
        assertThat(resultado.getLast().fin()).isEqualTo(sabado.atTime(13, 0).atZone(ZONA).toInstant());
    }

    @Test
    void noDebeGenerarFranjasEnDomingoNiFestivo() {
        LocalDate domingo = LocalDate.of(2026, 6, 14);
        LocalDate lunesFestivo = LocalDate.of(2026, 6, 15);
        when(festivos.esFestivo(lunesFestivo)).thenReturn(true);
        when(citas.buscarFranjasOcupadas(eq(medicoId), any(Instant.class), any(Instant.class)))
                .thenReturn(Set.of());

        var resultado = useCase.ejecutar(medicoId, domingo, lunesFestivo);

        assertThat(resultado).isEmpty();
    }

    @Test
    void debeExcluirFranjasPasadasYLaFranjaQueIniciaExactamenteAhora() {
        LocalDate miercoles = LocalDate.of(2026, 6, 10);
        Instant nueveLocal = miercoles.atTime(9, 0).atZone(ZONA).toInstant();
        var useCaseConHoraAvanzada = new ConsultarDisponibilidadUseCase(
                medicos, citas, new PoliticaHorarioAtencion(festivos),
                Clock.fixed(nueveLocal, ZoneOffset.UTC), ZONA, 90);
        when(citas.buscarFranjasOcupadas(eq(medicoId), any(Instant.class), any(Instant.class)))
                .thenReturn(Set.of());

        var resultado = useCaseConHoraAvanzada.ejecutar(medicoId, miercoles, miercoles);

        assertThat(resultado).hasSize(17);
        assertThat(resultado.getFirst().inicio())
                .isEqualTo(miercoles.atTime(9, 30).atZone(ZONA).toInstant());
    }

    @Test
    void debeRechazarRangoInvertido() {
        assertThatThrownBy(() -> useCase.ejecutar(
                medicoId, LocalDate.of(2026, 6, 12), LocalDate.of(2026, 6, 11)))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo("RANGO_FECHAS_INVALIDO");
    }

    @Test
    void debeRechazarMedicoInexistente() {
        when(medicos.buscarPorId(medicoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(
                medicoId, LocalDate.of(2026, 6, 11), LocalDate.of(2026, 6, 11)))
                .isInstanceOf(NotFoundException.class)
                .extracting(error -> ((NotFoundException) error).codigo())
                .isEqualTo("MEDICO_NO_ENCONTRADO");
    }

    @Test
    void debeAceptarExactamenteNoventaDiasCalendario() {
        LocalDate inicio = LocalDate.of(2026, 6, 11);
        LocalDate fin = inicio.plusDays(89);
        when(citas.buscarFranjasOcupadas(eq(medicoId), any(Instant.class), any(Instant.class)))
                .thenReturn(Set.of());

        var resultado = useCase.ejecutar(medicoId, inicio, fin);

        assertThat(resultado).isNotEmpty();
        verify(citas).buscarFranjasOcupadas(eq(medicoId), any(Instant.class), any(Instant.class));
    }

    @Test
    void debeRechazarNoventaYUnDiasAntesDeConsultarPersistencia() {
        LocalDate inicio = LocalDate.of(2026, 6, 11);

        assertThatThrownBy(() -> useCase.ejecutar(medicoId, inicio, inicio.plusDays(90)))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo("RANGO_DEMASIADO_AMPLIO");

        verify(medicos, never()).buscarPorId(medicoId);
        verify(citas, never()).buscarFranjasOcupadas(
                eq(medicoId), any(Instant.class), any(Instant.class));
    }
}
