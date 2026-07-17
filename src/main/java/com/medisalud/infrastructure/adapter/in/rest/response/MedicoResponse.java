package com.medisalud.infrastructure.adapter.in.rest.response;

import com.medisalud.application.dto.MedicoDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Medico registrado en MediSalud")
public record MedicoResponse(
        @Schema(example = "7dfc0f5a-2173-4cb0-97a5-8fb82db1e62c") UUID id,
        @Schema(example = "Dra. Maria Gonzalez") String nombreCompleto,
        @Schema(example = "Cardiologia") String especialidad,
        @Schema(example = "555-1001", nullable = true) String telefono,
        @Schema(example = "maria.gonzalez@medisalud.com", nullable = true) String email) {

    public static MedicoResponse desde(MedicoDto medico) {
        Objects.requireNonNull(medico, "El medico es obligatorio");
        return new MedicoResponse(medico.id(), medico.nombreCompleto(), medico.especialidad(),
                medico.telefono(), medico.email());
    }
}
