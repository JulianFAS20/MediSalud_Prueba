package com.medisalud.domain.port;

import com.medisalud.domain.model.Paciente;

import java.util.Optional;
import java.util.UUID;

public interface PacienteRepositoryPort {

    Paciente guardar(Paciente paciente);

    Optional<Paciente> buscarPorId(UUID id);

    boolean existePorDocumento(String documentoIdentidad);
}
