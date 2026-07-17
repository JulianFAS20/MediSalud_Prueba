package com.medisalud.domain.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record JornadaAtencion(LocalTime apertura, LocalTime cierre) {

    public JornadaAtencion {
        Objects.requireNonNull(apertura, "La hora de apertura es obligatoria");
        Objects.requireNonNull(cierre, "La hora de cierre es obligatoria");
        if (!apertura.isBefore(cierre)) {
            throw new IllegalArgumentException("La apertura debe ser anterior al cierre");
        }
        if (Duration.between(apertura, cierre).compareTo(FranjaHoraria.DURACION) < 0) {
            throw new IllegalArgumentException("La jornada debe permitir al menos una franja de atencion");
        }
    }

    public boolean contiene(LocalTime inicio) {
        Objects.requireNonNull(inicio, "La hora de inicio es obligatoria");
        return !inicio.isBefore(apertura)
                && inicio.isBefore(cierre)
                && !inicio.plus(FranjaHoraria.DURACION).isAfter(cierre);
    }

    public List<LocalTime> iniciosDeFranja() {
        List<LocalTime> inicios = new ArrayList<>();
        for (LocalTime hora = apertura; contiene(hora); hora = hora.plus(FranjaHoraria.DURACION)) {
            inicios.add(hora);
        }
        return List.copyOf(inicios);
    }
}
