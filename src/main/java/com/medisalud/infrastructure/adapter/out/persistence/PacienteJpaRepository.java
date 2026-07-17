package com.medisalud.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PacienteJpaRepository extends JpaRepository<PacienteJpaEntity, UUID> {

    boolean existsByDocumentoIdentidad(String documentoIdentidad);
}
