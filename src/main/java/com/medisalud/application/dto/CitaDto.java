package com.medisalud.application.dto;

import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.EstadoCita;

import java.time.Instant;
import java.util.UUID;

public record CitaDto(
        UUID id,
        UUID pacienteId,
        UUID medicoId,
        Instant fechaHora,
        Instant fechaHoraFin,
        EstadoCita estado,
        Instant canceladaEn) {

    public static CitaDto desde(Cita cita) {
        return new CitaDto(cita.id(), cita.pacienteId(), cita.medicoId(), cita.franja().inicio(),
                cita.franja().fin(), cita.estado(), cita.canceladaEn());
    }
}
