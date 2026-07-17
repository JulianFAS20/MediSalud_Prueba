package com.medisalud.infrastructure.adapter.in.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@Schema(description = "Nuevo horario solicitado para una cita")
public record ReprogramarCitaRequest(
        @Schema(example = "2027-02-01T10:00:00-05:00") @NotNull OffsetDateTime nuevaFechaHora) {
}
