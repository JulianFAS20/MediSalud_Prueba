package com.medisalud.infrastructure.adapter.in.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un medico")
public record RegistrarMedicoRequest(
        @Schema(example = "Dra. Maria Gonzalez")
        @NotBlank @Size(min = 3, max = 100) String nombreCompleto,
        @Schema(example = "Cardiologia")
        @NotBlank @Size(min = 2, max = 100) String especialidad,
        @Schema(example = "555-1001", nullable = true)
        @Pattern(regexp = "^(?:\\D*\\d){7,}\\D*$", message = "debe contener al menos 7 digitos") String telefono,
        @Schema(example = "maria.gonzalez@medisalud.com", nullable = true)
        @Email @Size(max = 254) String email) {
}
