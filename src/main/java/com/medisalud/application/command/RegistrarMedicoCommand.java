package com.medisalud.application.command;

public record RegistrarMedicoCommand(String nombreCompleto, String especialidad, String telefono, String email) {
}
