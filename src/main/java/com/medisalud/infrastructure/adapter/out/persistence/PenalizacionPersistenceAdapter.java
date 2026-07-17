package com.medisalud.infrastructure.adapter.out.persistence;

import com.medisalud.domain.model.Penalizacion;
import com.medisalud.domain.port.PenalizacionRepositoryPort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class PenalizacionPersistenceAdapter implements PenalizacionRepositoryPort {

    private final PenalizacionJpaRepository repository;

    public PenalizacionPersistenceAdapter(PenalizacionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Penalizacion guardar(Penalizacion penalizacion) {
        return repository.save(PenalizacionJpaEntity.desde(penalizacion)).aDominio();
    }

    @Override
    public long contarDesde(UUID pacienteId, Instant desdeInclusive) {
        return repository.countByPacienteIdAndRegistradaEnGreaterThanEqual(pacienteId, desdeInclusive);
    }
}
