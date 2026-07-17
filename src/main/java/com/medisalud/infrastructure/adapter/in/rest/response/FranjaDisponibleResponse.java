package com.medisalud.infrastructure.adapter.in.rest.response;

import com.medisalud.application.dto.FranjaDisponibleDto;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record FranjaDisponibleResponse(OffsetDateTime inicio, OffsetDateTime fin) {

    public static FranjaDisponibleResponse desde(FranjaDisponibleDto franja, ZoneId zonaHoraria) {
        return new FranjaDisponibleResponse(
                franja.inicio().atZone(zonaHoraria).toOffsetDateTime(),
                franja.fin().atZone(zonaHoraria).toOffsetDateTime());
    }
}
