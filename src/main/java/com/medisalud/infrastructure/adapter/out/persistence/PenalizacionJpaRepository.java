package com.medisalud.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface PenalizacionJpaRepository extends JpaRepository<PenalizacionJpaEntity, UUID> {

    long countByPacienteIdAndRegistradaEnGreaterThanEqual(UUID pacienteId, Instant desdeInclusive);
}
