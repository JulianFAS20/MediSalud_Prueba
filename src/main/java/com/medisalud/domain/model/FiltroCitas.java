package com.medisalud.domain.model;

import java.time.Instant;
import java.util.UUID;

public record FiltroCitas(
        UUID medicoId,
        UUID pacienteId,
        EstadoCita estado,
        Instant fechaInicio,
        Instant fechaFin) {
}
