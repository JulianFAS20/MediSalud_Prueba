package com.medisalud.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Penalizacion(UUID id, UUID pacienteId, UUID citaId, Instant registradaEn, String motivo) {

    public Penalizacion {
        Objects.requireNonNull(id, "El id de la penalizacion es obligatorio");
        Objects.requireNonNull(pacienteId, "El paciente es obligatorio");
        Objects.requireNonNull(citaId, "La cita es obligatoria");
        Objects.requireNonNull(registradaEn, "La fecha de penalizacion es obligatoria");
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la penalizacion es obligatorio");
        }
    }

    public static Penalizacion porCancelacionTardia(UUID pacienteId, UUID citaId, Instant registradaEn) {
        return new Penalizacion(UUID.randomUUID(), pacienteId, citaId, registradaEn, "Cancelacion con menos de 2 horas");
    }
}
