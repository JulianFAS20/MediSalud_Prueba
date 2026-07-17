package com.medisalud.infrastructure.adapter.in.rest.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Formato uniforme de error de la API")
public record ApiError(
        @Schema(example = "2026-06-10T15:30:00Z") Instant timestamp,
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "REQUEST_INVALIDO") String codigo,
        @Schema(example = "La solicitud contiene campos invalidos") String mensaje,
        @Schema(example = "/api/v1/pacientes") String path,
        @Schema(nullable = true) List<CampoError> erroresCampo) {
}
