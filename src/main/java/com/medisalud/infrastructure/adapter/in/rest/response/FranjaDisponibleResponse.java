package com.medisalud.infrastructure.adapter.in.rest.response;

import com.medisalud.application.dto.FranjaDisponibleDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Schema(description = "Franja de atencion disponible de 30 minutos")
public record FranjaDisponibleResponse(
        @Schema(example = "2027-02-01T08:00:00-05:00") OffsetDateTime inicio,
        @Schema(example = "2027-02-01T08:30:00-05:00") OffsetDateTime fin) {

    public static FranjaDisponibleResponse desde(FranjaDisponibleDto franja, ZoneId zonaHoraria) {
        return new FranjaDisponibleResponse(
                franja.inicio().atZone(zonaHoraria).toOffsetDateTime(),
                franja.fin().atZone(zonaHoraria).toOffsetDateTime());
    }
}
