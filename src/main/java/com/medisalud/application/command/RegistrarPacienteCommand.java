package com.medisalud.application.command;

import java.time.LocalDate;

public record RegistrarPacienteCommand(
        String nombreCompleto,
        String documentoIdentidad,
        String telefono,
        String email,
        LocalDate fechaNacimiento) {
}
