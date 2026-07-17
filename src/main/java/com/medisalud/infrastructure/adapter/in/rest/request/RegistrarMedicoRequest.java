package com.medisalud.infrastructure.adapter.in.rest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrarMedicoRequest(
        @NotBlank @Size(min = 3, max = 100) String nombreCompleto,
        @NotBlank @Size(min = 2, max = 100) String especialidad,
        @Pattern(regexp = "^(?:\\D*\\d){7,}\\D*$", message = "debe contener al menos 7 digitos") String telefono,
        @Email @Size(max = 254) String email) {
}
