package com.medisalud.infrastructure.adapter.in.rest.response;

import com.medisalud.application.dto.FranjaDisponibleDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Schema(description = "Resumen de franjas disponibles para el rango consultado")
public record DisponibilidadResponse(
        @Schema(example = "2") int cantidadFranjasDisponibles,
        List<FranjaDisponibleResponse> franjasDisponibles) {

    public DisponibilidadResponse {
        franjasDisponibles = List.copyOf(Objects.requireNonNull(
                franjasDisponibles, "Las franjas disponibles son obligatorias"));
        if (cantidadFranjasDisponibles != franjasDisponibles.size()) {
            throw new IllegalArgumentException(
                    "La cantidad de franjas debe coincidir con el tamano de la lista");
        }
    }

    public static DisponibilidadResponse desde(List<FranjaDisponibleDto> franjas, ZoneId zonaHoraria) {
        Objects.requireNonNull(franjas, "Las franjas disponibles son obligatorias");
        Objects.requireNonNull(zonaHoraria, "La zona horaria es obligatoria");
        List<FranjaDisponibleResponse> respuestas = franjas.stream()
                .map(franja -> FranjaDisponibleResponse.desde(franja, zonaHoraria))
                .toList();
        return new DisponibilidadResponse(respuestas.size(), respuestas);
    }
}
