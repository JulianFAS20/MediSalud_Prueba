package com.medisalud.infrastructure.adapter.in.rest.response;

import com.medisalud.application.dto.PacienteDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Paciente registrado en MediSalud")
public record PacienteResponse(
        @Schema(example = "6b3e3b0c-6296-4468-8163-2cf66882d81b") UUID id,
        @Schema(example = "Laura Martinez") String nombreCompleto,
        @Schema(example = "CC-1032456789") String documentoIdentidad,
        @Schema(example = "3001234567") String telefono,
        @Schema(example = "laura.martinez@example.com") String email,
        @Schema(example = "1992-05-18", nullable = true) LocalDate fechaNacimiento) {

    public static PacienteResponse desde(PacienteDto paciente) {
        Objects.requireNonNull(paciente, "El paciente es obligatorio");
        return new PacienteResponse(paciente.id(), paciente.nombreCompleto(), paciente.documentoIdentidad(),
                paciente.telefono(), paciente.email(), paciente.fechaNacimiento());
    }
}
