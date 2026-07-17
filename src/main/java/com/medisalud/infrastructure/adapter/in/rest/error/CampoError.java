package com.medisalud.infrastructure.adapter.in.rest.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de validacion asociado a un campo")
public record CampoError(
        @Schema(example = "email") String campo,
        @Schema(example = "debe ser una direccion de correo valida") String mensaje) {
}
