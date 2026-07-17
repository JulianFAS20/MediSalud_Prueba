package com.medisalud.application.validation;

import com.medisalud.domain.model.FranjaHoraria;
import com.medisalud.domain.model.Medico;
import com.medisalud.domain.model.Paciente;

import java.time.Instant;
import java.time.ZoneId;

public record ReservaValidationContext(
        Paciente paciente,
        Medico medico,
        FranjaHoraria franja,
        Instant ahora,
        ZoneId zonaHoraria) {
}
