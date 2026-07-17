package com.medisalud.domain.port;

import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.FiltroCitas;
import com.medisalud.domain.model.Pagina;
import com.medisalud.domain.model.Paginacion;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CitaRepositoryPort {

    Cita guardar(Cita cita);

    Optional<Cita> buscarPorId(UUID id);

    boolean existeProgramadaParaMedico(UUID medicoId, Instant fechaHora);

    boolean existeProgramadaParaPaciente(UUID pacienteId, Instant fechaHora);

    Set<Instant> buscarFranjasOcupadas(UUID medicoId, Instant desdeInclusive, Instant hastaExclusivo);

    Pagina<Cita> buscar(FiltroCitas filtro, Paginacion paginacion);
}
