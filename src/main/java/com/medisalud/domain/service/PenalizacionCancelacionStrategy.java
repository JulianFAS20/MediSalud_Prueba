package com.medisalud.domain.service;

import com.medisalud.domain.model.Cita;

import java.time.Instant;

public interface PenalizacionCancelacionStrategy {

    boolean debePenalizar(Cita cita, Instant fechaCancelacion);
}
