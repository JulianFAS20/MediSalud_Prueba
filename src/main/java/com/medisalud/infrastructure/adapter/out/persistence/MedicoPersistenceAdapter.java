package com.medisalud.infrastructure.adapter.out.persistence;

import com.medisalud.domain.model.Medico;
import com.medisalud.domain.port.MedicoRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class MedicoPersistenceAdapter implements MedicoRepositoryPort {

    private final MedicoJpaRepository repository;

    public MedicoPersistenceAdapter(MedicoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Medico guardar(Medico medico) {
        return repository.save(MedicoJpaEntity.desde(medico)).aDominio();
    }

    @Override
    public Optional<Medico> buscarPorId(UUID id) {
        return repository.findById(id).map(MedicoJpaEntity::aDominio);
    }

    @Override
    public long contar() {
        return repository.count();
    }
}
