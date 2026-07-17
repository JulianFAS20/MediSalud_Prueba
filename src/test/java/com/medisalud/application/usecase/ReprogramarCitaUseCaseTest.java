package com.medisalud.application.usecase;

import com.medisalud.application.command.ReservarCitaCommand;
import com.medisalud.application.dto.CitaDto;
import com.medisalud.application.port.TransaccionPort;
import com.medisalud.domain.exception.NotFoundException;
import com.medisalud.domain.exception.ValidationException;
import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.FranjaHoraria;
import com.medisalud.domain.port.CitaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReprogramarCitaUseCaseTest {

    private static final Instant AHORA = Instant.parse("2026-06-10T12:00:00Z");
    private static final Instant FECHA_ACTUAL = Instant.parse("2026-06-10T14:00:00Z");
    private static final Instant NUEVA_FECHA = Instant.parse("2026-06-11T14:00:00Z");

    @Mock
    private CitaRepositoryPort citas;
    @Mock
    private CancelarCitaUseCase cancelarCita;
    @Mock
    private ReservarCitaUseCase reservarCita;

    private Cita anterior;
    private ReprogramarCitaUseCase useCase;

    @BeforeEach
    void configurar() {
        anterior = Cita.programar(UUID.randomUUID(), UUID.randomUUID(), new FranjaHoraria(FECHA_ACTUAL));
        useCase = new ReprogramarCitaUseCase(citas, cancelarCita, reservarCita,
                new TransaccionSincrona(), Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    void debeCancelarLaAnteriorYReservarOtraConLosMismosParticipantes() {
        when(citas.buscarPorId(anterior.id())).thenReturn(Optional.of(anterior));
        Cita nueva = Cita.programar(anterior.pacienteId(), anterior.medicoId(), new FranjaHoraria(NUEVA_FECHA));
        when(reservarCita.reservar(
                org.mockito.ArgumentMatchers.any(ReservarCitaCommand.class),
                org.mockito.ArgumentMatchers.eq(AHORA)))
                .thenReturn(CitaDto.desde(nueva));

        CitaDto resultado = useCase.ejecutar(anterior.id(), NUEVA_FECHA);

        verify(cancelarCita).cancelar(anterior.id(), AHORA);
        ArgumentCaptor<ReservarCitaCommand> command = ArgumentCaptor.forClass(ReservarCitaCommand.class);
        verify(reservarCita).reservar(command.capture(), org.mockito.ArgumentMatchers.eq(AHORA));
        assertThat(command.getValue()).isEqualTo(
                new ReservarCitaCommand(anterior.pacienteId(), anterior.medicoId(), NUEVA_FECHA));
        assertThat(resultado.id()).isEqualTo(nueva.id());
        assertThat(resultado.fechaHora()).isEqualTo(NUEVA_FECHA);
    }

    @Test
    void debeRechazarLaMismaFranjaSinCancelarLaCita() {
        when(citas.buscarPorId(anterior.id())).thenReturn(Optional.of(anterior));

        assertThatThrownBy(() -> useCase.ejecutar(anterior.id(), FECHA_ACTUAL))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo("MISMA_FRANJA");

        verify(cancelarCita, never()).cancelar(
                org.mockito.ArgumentMatchers.any(UUID.class), org.mockito.ArgumentMatchers.any(Instant.class));
        verify(reservarCita, never()).reservar(
                org.mockito.ArgumentMatchers.any(ReservarCitaCommand.class),
                org.mockito.ArgumentMatchers.any(Instant.class));
    }

    @Test
    void debeRechazarCitaInexistenteAntesDeCancelar() {
        when(citas.buscarPorId(anterior.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(anterior.id(), NUEVA_FECHA))
                .isInstanceOf(NotFoundException.class)
                .extracting(error -> ((NotFoundException) error).codigo())
                .isEqualTo("CITA_NO_ENCONTRADA");

        verify(cancelarCita, never()).cancelar(
                org.mockito.ArgumentMatchers.any(UUID.class), org.mockito.ArgumentMatchers.any(Instant.class));
    }

    private static final class TransaccionSincrona implements TransaccionPort {
        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            return operacion.get();
        }
    }
}
