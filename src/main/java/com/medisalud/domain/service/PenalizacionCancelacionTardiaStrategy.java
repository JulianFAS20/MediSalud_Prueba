package com.medisalud.domain.service;

import com.medisalud.domain.model.Cita;

import java.time.Duration;
import java.time.Instant;

public final class PenalizacionCancelacionTardiaStrategy implements PenalizacionCancelacionStrategy {

    private static final Duration ANTELACION_MINIMA = Duration.ofHours(2);

    @Override
    public boolean debePenalizar(Cita cita, Instant fechaCancelacion) {
        Duration antelacion = Duration.between(fechaCancelacion, cita.franja().inicio());
        return antelacion.compareTo(ANTELACION_MINIMA) < 0;
    }
}
