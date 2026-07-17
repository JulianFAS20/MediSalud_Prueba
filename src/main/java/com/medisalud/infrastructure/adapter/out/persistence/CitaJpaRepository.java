package com.medisalud.infrastructure.adapter.out.persistence;

import com.medisalud.domain.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CitaJpaRepository extends JpaRepository<CitaJpaEntity, UUID>, JpaSpecificationExecutor<CitaJpaEntity> {

    boolean existsByMedicoIdAndFechaHoraAndEstado(UUID medicoId, Instant fechaHora, EstadoCita estado);

    boolean existsByPacienteIdAndFechaHoraAndEstado(UUID pacienteId, Instant fechaHora, EstadoCita estado);

    @Query("""
            select c.fechaHora from CitaJpaEntity c
            where c.medicoId = :medicoId
              and c.estado = :estado
              and c.fechaHora >= :desde
              and c.fechaHora < :hasta
            """)
    List<Instant> buscarFranjas(
            @Param("medicoId") UUID medicoId,
            @Param("estado") EstadoCita estado,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta);
}
