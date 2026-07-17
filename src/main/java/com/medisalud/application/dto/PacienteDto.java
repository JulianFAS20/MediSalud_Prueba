package com.medisalud.application.dto;

import com.medisalud.domain.model.Paciente;

import java.time.LocalDate;
import java.util.UUID;

public record PacienteDto(
        UUID id,
        String nombreCompleto,
        String documentoIdentidad,
        String telefono,
        String email,
        LocalDate fechaNacimiento) {

    public static PacienteDto desde(Paciente paciente) {
        return new PacienteDto(paciente.id(), paciente.nombreCompleto(), paciente.documentoIdentidad(),
                paciente.telefono(), paciente.email(), paciente.fechaNacimiento());
    }
}
