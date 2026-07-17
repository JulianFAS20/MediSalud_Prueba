package com.medisalud.application.validation;

import com.medisalud.domain.exception.ConflictException;
import com.medisalud.domain.port.PenalizacionRepositoryPort;

import java.time.Duration;

public final class PenalizacionesActivasValidator implements ReservaValidator {

    private static final int MAXIMO_PENALIZACIONES = 3;
    private static final Duration VENTANA = Duration.ofDays(30);

    private final PenalizacionRepositoryPort penalizaciones;

    public PenalizacionesActivasValidator(PenalizacionRepositoryPort penalizaciones) {
        this.penalizaciones = penalizaciones;
    }

    @Override
    public void validar(ReservaValidationContext contexto) {
        long total = penalizaciones.contarDesde(contexto.paciente().id(), contexto.ahora().minus(VENTANA));
        if (total >= MAXIMO_PENALIZACIONES) {
            throw new ConflictException("PACIENTE_BLOQUEADO",
                    "El paciente tiene 3 o mas penalizaciones en los ultimos 30 dias");
        }
    }
}
