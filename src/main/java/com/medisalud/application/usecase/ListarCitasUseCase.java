package com.medisalud.application.usecase;

import com.medisalud.application.dto.CitaDto;
import com.medisalud.domain.exception.ValidationException;
import com.medisalud.domain.model.EstadoCita;
import com.medisalud.domain.model.FiltroCitas;
import com.medisalud.domain.port.CitaRepositoryPort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ListarCitasUseCase {

    private final CitaRepositoryPort citas;

    public ListarCitasUseCase(CitaRepositoryPort citas) {
        this.citas = citas;
    }

    public List<CitaDto> ejecutar(UUID medicoId, UUID pacienteId, EstadoCita estado,
                                  Instant fechaInicio, Instant fechaFin) {
        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            throw new ValidationException("RANGO_FECHAS_INVALIDO", "fechaInicio no puede ser posterior a fechaFin");
        }
        return citas.buscar(new FiltroCitas(medicoId, pacienteId, estado, fechaInicio, fechaFin))
                .stream()
                .map(CitaDto::desde)
                .toList();
    }
}
