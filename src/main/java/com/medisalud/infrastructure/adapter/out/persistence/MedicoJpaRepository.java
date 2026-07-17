package com.medisalud.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MedicoJpaRepository extends JpaRepository<MedicoJpaEntity, UUID> {
}
