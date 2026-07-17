package com.medisalud.domain.port;

import com.medisalud.domain.model.Medico;

import java.util.Optional;
import java.util.UUID;

public interface MedicoRepositoryPort {

    Medico guardar(Medico medico);

    Optional<Medico> buscarPorId(UUID id);

    long contar();
}
