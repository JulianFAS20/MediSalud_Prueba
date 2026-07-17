package com.medisalud.application.validation;

import com.medisalud.domain.exception.ConflictException;
import com.medisalud.domain.port.CitaRepositoryPort;

public final class DisponibilidadMedicoValidator implements ReservaValidator {

    private final CitaRepositoryPort citas;

    public DisponibilidadMedicoValidator(CitaRepositoryPort citas) {
        this.citas = citas;
    }

    @Override
    public void validar(ReservaValidationContext contexto) {
        if (citas.existeProgramadaParaMedico(contexto.medico().id(), contexto.franja().inicio())) {
            throw new ConflictException("FRANJA_MEDICO_OCUPADA",
                    "El medico ya tiene una cita programada en esa franja");
        }
    }
}
