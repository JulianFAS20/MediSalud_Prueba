package com.medisalud.application.usecase;

import com.medisalud.application.port.TransaccionPort;
import com.medisalud.domain.exception.ConflictException;
import com.medisalud.domain.exception.NotFoundException;
import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.EstadoCita;
import com.medisalud.domain.model.FranjaHoraria;
import com.medisalud.domain.model.Penalizacion;
import com.medisalud.domain.port.CitaRepositoryPort;
import com.medisalud.domain.port.PenalizacionRepositoryPort;
import com.medisalud.domain.service.PenalizacionCancelacionTardiaStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarCitaUseCaseTest {

    @Mock
    private CitaRepositoryPort citas;
    @Mock
    private PenalizacionRepositoryPort penalizaciones;

    @Test
    void debeCancelarYPenalizarConMenosDeDosHoras() {
        Instant ahora = Instant.parse("2026-06-10T14:00:00Z");
        Cita cita = Cita.programar(UUID.randomUUID(), UUID.randomUUID(),
                new FranjaHoraria(Instant.parse("2026-06-10T15:30:00Z")));
        when(citas.buscarPorId(cita.id())).thenReturn(Optional.of(cita));
        when(citas.guardar(cita)).thenReturn(cita);
        when(penalizaciones.guardar(any(Penalizacion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        var useCase = new CancelarCitaUseCase(
                citas,
                penalizaciones,
                new PenalizacionCancelacionTardiaStrategy(),
                new TransaccionSincrona(),
                Clock.fixed(ahora, ZoneOffset.UTC));

        var resultado = useCase.ejecutar(cita.id());

        assertThat(resultado.estado()).isEqualTo(EstadoCita.CANCELADA);
        assertThat(resultado.canceladaEn()).isEqualTo(ahora);
        verify(penalizaciones).guardar(any(Penalizacion.class));
    }

    @Test
    void noDebePenalizarCuandoSeCancelaConDosHorasExactas() {
        Instant fechaCita = Instant.parse("2026-06-10T16:00:00Z");
        Instant ahora = fechaCita.minusSeconds(2 * 60 * 60L);
        Cita cita = Cita.programar(UUID.randomUUID(), UUID.randomUUID(), new FranjaHoraria(fechaCita));
        when(citas.buscarPorId(cita.id())).thenReturn(Optional.of(cita));
        when(citas.guardar(cita)).thenReturn(cita);

        var useCase = crearUseCase(ahora);

        var resultado = useCase.ejecutar(cita.id());

        assertThat(resultado.estado()).isEqualTo(EstadoCita.CANCELADA);
        verify(penalizaciones, never()).guardar(any(Penalizacion.class));
    }

    @Test
    void debeFallarCuandoLaCitaNoExiste() {
        UUID citaId = UUID.randomUUID();
        when(citas.buscarPorId(citaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> crearUseCase(Instant.parse("2026-06-10T14:00:00Z")).ejecutar(citaId))
                .isInstanceOf(NotFoundException.class)
                .extracting(error -> ((NotFoundException) error).codigo())
                .isEqualTo("CITA_NO_ENCONTRADA");

        verify(citas, never()).guardar(any(Cita.class));
        verify(penalizaciones, never()).guardar(any(Penalizacion.class));
    }

    @Test
    void debeRechazarLaCancelacionDeUnaCitaYaCanceladaSinDuplicarPenalizacion() {
        Instant ahora = Instant.parse("2026-06-10T14:00:00Z");
        Cita cita = Cita.restaurar(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new FranjaHoraria(Instant.parse("2026-06-10T15:00:00Z")),
                EstadoCita.CANCELADA, ahora.minusSeconds(60));
        when(citas.buscarPorId(cita.id())).thenReturn(Optional.of(cita));

        assertThatThrownBy(() -> crearUseCase(ahora).ejecutar(cita.id()))
                .isInstanceOf(ConflictException.class)
                .extracting(error -> ((ConflictException) error).codigo())
                .isEqualTo("CITA_NO_PROGRAMADA");

        verify(citas, never()).guardar(any(Cita.class));
        verify(penalizaciones, never()).guardar(any(Penalizacion.class));
    }

    private CancelarCitaUseCase crearUseCase(Instant ahora) {
        return new CancelarCitaUseCase(
                citas,
                penalizaciones,
                new PenalizacionCancelacionTardiaStrategy(),
                new TransaccionSincrona(),
                Clock.fixed(ahora, ZoneOffset.UTC));
    }

    private static final class TransaccionSincrona implements TransaccionPort {
        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            return operacion.get();
        }
    }
}
