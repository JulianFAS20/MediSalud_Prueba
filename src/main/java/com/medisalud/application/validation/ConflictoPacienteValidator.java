package com.medisalud.application.validation;

import com.medisalud.domain.exception.ConflictException;
import com.medisalud.domain.port.CitaRepositoryPort;

public final class ConflictoPacienteValidator implements ReservaValidator {

    private final CitaRepositoryPort citas;

    public ConflictoPacienteValidator(CitaRepositoryPort citas) {
        this.citas = citas;
    }

    @Override
    public void validar(ReservaValidationContext contexto) {
        if (citas.existeProgramadaParaPaciente(contexto.paciente().id(), contexto.franja().inicio())) {
            throw new ConflictException("FRANJA_PACIENTE_OCUPADA",
                    "El paciente ya tiene una cita programada en esa franja");
        }
    }
}
