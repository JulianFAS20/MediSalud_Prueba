package com.medisalud.infrastructure.adapter.out.persistence;

import com.medisalud.domain.model.Paciente;
import com.medisalud.domain.port.PacienteRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PacientePersistenceAdapter implements PacienteRepositoryPort {

    private final PacienteJpaRepository repository;

    public PacientePersistenceAdapter(PacienteJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Paciente guardar(Paciente paciente) {
        return repository.save(PacienteJpaEntity.desde(paciente)).aDominio();
    }

    @Override
    public Optional<Paciente> buscarPorId(UUID id) {
        return repository.findById(id).map(PacienteJpaEntity::aDominio);
    }

    @Override
    public boolean existePorDocumento(String documentoIdentidad) {
        return documentoIdentidad != null && repository.existsByDocumentoIdentidad(documentoIdentidad.trim());
    }
}
