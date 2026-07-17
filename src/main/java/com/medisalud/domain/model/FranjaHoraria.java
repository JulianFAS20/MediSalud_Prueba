package com.medisalud.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record FranjaHoraria(Instant inicio) {

    public static final Duration DURACION = Duration.ofMinutes(30);

    public FranjaHoraria {
        Objects.requireNonNull(inicio, "El inicio de la franja es obligatorio");
    }

    public Instant fin() {
        return inicio.plus(DURACION);
    }
}
