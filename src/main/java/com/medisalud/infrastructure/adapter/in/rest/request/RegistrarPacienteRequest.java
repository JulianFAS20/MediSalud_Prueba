package com.medisalud.infrastructure.adapter.in.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Datos para registrar un paciente")
public record RegistrarPacienteRequest(
        @Schema(example = "Laura Martinez")
        @NotBlank @Size(min = 3, max = 100) String nombreCompleto,
        @Schema(example = "CC-1032456789")
        @NotBlank @Size(min = 7, max = 50) String documentoIdentidad,
        @Schema(example = "3001234567")
        @NotBlank @Pattern(regexp = "^(?:\\D*\\d){7,}\\D*$", message = "debe contener al menos 7 digitos") String telefono,
        @Schema(example = "laura.martinez@example.com")
        @NotBlank @Email @Size(max = 254) String email,
        @Schema(example = "1992-05-18", nullable = true) LocalDate fechaNacimiento) {
}
