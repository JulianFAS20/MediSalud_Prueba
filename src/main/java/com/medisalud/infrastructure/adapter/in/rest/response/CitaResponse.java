package com.medisalud.infrastructure.adapter.in.rest.response;

import com.medisalud.application.dto.CitaDto;
import com.medisalud.domain.model.EstadoCita;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

public record CitaResponse(
        UUID id,
        UUID pacienteId,
        UUID medicoId,
        OffsetDateTime fechaHora,
        OffsetDateTime fechaHoraFin,
        EstadoCita estado,
        OffsetDateTime canceladaEn) {

    public static CitaResponse desde(CitaDto cita, ZoneId zonaHoraria) {
        return new CitaResponse(
                cita.id(),
                cita.pacienteId(),
                cita.medicoId(),
                cita.fechaHora().atZone(zonaHoraria).toOffsetDateTime(),
                cita.fechaHoraFin().atZone(zonaHoraria).toOffsetDateTime(),
                cita.estado(),
                cita.canceladaEn() == null ? null : cita.canceladaEn().atZone(zonaHoraria).toOffsetDateTime());
    }
}
