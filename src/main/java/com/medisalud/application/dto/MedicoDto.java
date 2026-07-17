package com.medisalud.application.dto;

import com.medisalud.domain.model.Medico;

import java.util.UUID;

public record MedicoDto(UUID id, String nombreCompleto, String especialidad, String telefono, String email) {

    public static MedicoDto desde(Medico medico) {
        return new MedicoDto(medico.id(), medico.nombreCompleto(), medico.especialidad(), medico.telefono(), medico.email());
    }
}
