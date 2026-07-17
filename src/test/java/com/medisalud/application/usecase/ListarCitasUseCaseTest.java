package com.medisalud.application.usecase;

import com.medisalud.domain.exception.ValidationException;
import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.EstadoCita;
import com.medisalud.domain.model.FiltroCitas;
import com.medisalud.domain.model.FranjaHoraria;
import com.medisalud.domain.port.CitaRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarCitasUseCaseTest {

    @Mock
    private CitaRepositoryPort citas;

    @Test
    void debeCombinarFiltrosYMapearResultados() {
        UUID medicoId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        Instant desde = Instant.parse("2026-06-10T13:00:00Z");
        Instant hasta = Instant.parse("2026-06-10T18:00:00Z");
        Cita cita = Cita.programar(pacienteId, medicoId,
                new FranjaHoraria(Instant.parse("2026-06-10T14:00:00Z")));
        when(citas.buscar(org.mockito.ArgumentMatchers.any(FiltroCitas.class))).thenReturn(List.of(cita));

        var resultado = new ListarCitasUseCase(citas)
                .ejecutar(medicoId, pacienteId, EstadoCita.PROGRAMADA, desde, hasta);

        ArgumentCaptor<FiltroCitas> filtro = ArgumentCaptor.forClass(FiltroCitas.class);
        verify(citas).buscar(filtro.capture());
        assertThat(filtro.getValue()).isEqualTo(
                new FiltroCitas(medicoId, pacienteId, EstadoCita.PROGRAMADA, desde, hasta));
        assertThat(resultado).singleElement().satisfies(dto -> {
            assertThat(dto.id()).isEqualTo(cita.id());
            assertThat(dto.fechaHoraFin()).isEqualTo(cita.franja().fin());
        });
    }

    @Test
    void debePermitirLimitesParciales() {
        Instant desde = Instant.parse("2026-06-10T13:00:00Z");
        when(citas.buscar(org.mockito.ArgumentMatchers.any(FiltroCitas.class))).thenReturn(List.of());

        assertThat(new ListarCitasUseCase(citas).ejecutar(null, null, null, desde, null)).isEmpty();

        ArgumentCaptor<FiltroCitas> filtro = ArgumentCaptor.forClass(FiltroCitas.class);
        verify(citas).buscar(filtro.capture());
        assertThat(filtro.getValue().fechaInicio()).isEqualTo(desde);
        assertThat(filtro.getValue().fechaFin()).isNull();
    }

    @Test
    void debeRechazarRangoInvertidoAntesDeConsultarPersistencia() {
        Instant desde = Instant.parse("2026-06-10T18:00:00Z");
        Instant hasta = Instant.parse("2026-06-10T13:00:00Z");

        assertThatThrownBy(() ->
                new ListarCitasUseCase(citas).ejecutar(null, null, null, desde, hasta))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo("RANGO_FECHAS_INVALIDO");

        verify(citas, never()).buscar(org.mockito.ArgumentMatchers.any(FiltroCitas.class));
    }
}
