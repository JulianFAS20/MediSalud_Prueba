package com.medisalud.infrastructure.adapter.in.rest.response;

import com.medisalud.application.dto.CitaDto;
import com.medisalud.domain.model.EstadoCita;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Schema(description = "Cita medica")
public record CitaResponse(
        @Schema(example = "f37d3d33-29a0-40aa-971a-721632c18c47") UUID id,
        @Schema(example = "6b3e3b0c-6296-4468-8163-2cf66882d81b") UUID pacienteId,
        @Schema(example = "7dfc0f5a-2173-4cb0-97a5-8fb82db1e62c") UUID medicoId,
        @Schema(example = "2027-02-01T08:30:00-05:00") OffsetDateTime fechaHora,
        @Schema(example = "2027-02-01T09:00:00-05:00") OffsetDateTime fechaHoraFin,
        @Schema(example = "PROGRAMADA") EstadoCita estado,
        @Schema(example = "2027-02-01T07:15:00-05:00", nullable = true) OffsetDateTime canceladaEn) {

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
