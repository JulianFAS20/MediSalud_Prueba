package com.medisalud.domain.service;

import com.medisalud.domain.model.FranjaHoraria;
import com.medisalud.domain.model.JornadaAtencion;
import com.medisalud.domain.port.CalendarioFestivosPort;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PoliticaHorarioAtencion {

    private static final JornadaAtencion JORNADA_SEMANA =
            new JornadaAtencion(LocalTime.of(8, 0), LocalTime.of(18, 0));
    private static final JornadaAtencion JORNADA_SABADO =
            new JornadaAtencion(LocalTime.of(8, 0), LocalTime.of(13, 0));

    private final CalendarioFestivosPort calendarioFestivos;

    public PoliticaHorarioAtencion(CalendarioFestivosPort calendarioFestivos) {
        this.calendarioFestivos = Objects.requireNonNull(
                calendarioFestivos, "El calendario de festivos es obligatorio");
    }

    public Optional<JornadaAtencion> jornadaPara(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha es obligatoria");
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY || calendarioFestivos.esFestivo(fecha)) {
            return Optional.empty();
        }
        return Optional.of(fecha.getDayOfWeek() == DayOfWeek.SATURDAY
                ? JORNADA_SABADO
                : JORNADA_SEMANA);
    }

    public boolean esInicioAlineado(LocalTime inicio) {
        Objects.requireNonNull(inicio, "La hora de inicio es obligatoria");
        long desplazamientoDesdeApertura = Duration.between(JORNADA_SEMANA.apertura(), inicio).toNanos();
        return desplazamientoDesdeApertura % FranjaHoraria.DURACION.toNanos() == 0;
    }

    public List<LocalTime> iniciosDeFranja(LocalDate fecha) {
        return jornadaPara(fecha)
                .map(JornadaAtencion::iniciosDeFranja)
                .orElseGet(List::of);
    }
}
