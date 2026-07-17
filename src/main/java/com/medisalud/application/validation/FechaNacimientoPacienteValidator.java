package com.medisalud.application.validation;

public final class FechaNacimientoPacienteValidator implements ReservaValidator {

    @Override
    public void validar(ReservaValidationContext contexto) {
        contexto.paciente().validarFechaNacimiento(contexto.ahora().atZone(contexto.zonaHoraria()).toLocalDate());
    }
}
