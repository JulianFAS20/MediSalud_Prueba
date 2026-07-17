package com.medisalud.domain.port;

import com.medisalud.domain.model.Penalizacion;

import java.time.Instant;
import java.util.UUID;

public interface PenalizacionRepositoryPort {

    Penalizacion guardar(Penalizacion penalizacion);

    long contarDesde(UUID pacienteId, Instant desdeInclusive);
}
