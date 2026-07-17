package com.medisalud.infrastructure.adapter.out.persistence;

import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.EstadoCita;
import com.medisalud.domain.model.FiltroCitas;
import com.medisalud.domain.port.CitaRepositoryPort;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CitaPersistenceAdapter implements CitaRepositoryPort {

    private final CitaJpaRepository repository;

    public CitaPersistenceAdapter(CitaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Cita guardar(Cita cita) {
        CitaJpaEntity entity = repository.findById(cita.id()).orElseGet(() -> CitaJpaEntity.desde(cita));
        entity.actualizarDesde(cita);
        return repository.save(entity).aDominio();
    }

    @Override
    public Optional<Cita> buscarPorId(UUID id) {
        return repository.findById(id).map(CitaJpaEntity::aDominio);
    }

    @Override
    public boolean existeProgramadaParaMedico(UUID medicoId, Instant fechaHora) {
        return repository.existsByMedicoIdAndFechaHoraAndEstado(medicoId, fechaHora, EstadoCita.PROGRAMADA);
    }

    @Override
    public boolean existeProgramadaParaPaciente(UUID pacienteId, Instant fechaHora) {
        return repository.existsByPacienteIdAndFechaHoraAndEstado(pacienteId, fechaHora, EstadoCita.PROGRAMADA);
    }

    @Override
    public Set<Instant> buscarFranjasOcupadas(UUID medicoId, Instant desdeInclusive, Instant hastaExclusivo) {
        return repository.buscarFranjas(medicoId, EstadoCita.PROGRAMADA, desdeInclusive, hastaExclusivo)
                .stream()
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public List<Cita> buscar(FiltroCitas filtro) {
        Specification<CitaJpaEntity> especificacion = Specification.allOf();
        if (filtro.medicoId() != null) {
            especificacion = especificacion.and((root, query, cb) -> cb.equal(root.get("medicoId"), filtro.medicoId()));
        }
        if (filtro.pacienteId() != null) {
            especificacion = especificacion.and((root, query, cb) -> cb.equal(root.get("pacienteId"), filtro.pacienteId()));
        }
        if (filtro.estado() != null) {
            especificacion = especificacion.and((root, query, cb) -> cb.equal(root.get("estado"), filtro.estado()));
        }
        if (filtro.fechaInicio() != null) {
            especificacion = especificacion.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("fechaHora"), filtro.fechaInicio()));
        }
        if (filtro.fechaFin() != null) {
            especificacion = especificacion.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("fechaHora"), filtro.fechaFin()));
        }
        return repository.findAll(especificacion, Sort.by(Sort.Direction.ASC, "fechaHora"))
                .stream()
                .map(CitaJpaEntity::aDominio)
                .toList();
    }
}
