package com.medisalud.application.usecase;

import com.medisalud.application.dto.FranjaDisponibleDto;
import com.medisalud.domain.exception.NotFoundException;
import com.medisalud.domain.exception.ValidationException;
import com.medisalud.domain.port.CalendarioFestivosPort;
import com.medisalud.domain.port.CitaRepositoryPort;
import com.medisalud.domain.port.MedicoRepositoryPort;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ConsultarDisponibilidadUseCase {

    private static final LocalTime APERTURA = LocalTime.of(8, 0);
    private static final LocalTime CIERRE_SEMANA = LocalTime.of(18, 0);
    private static final LocalTime CIERRE_SABADO = LocalTime.of(13, 0);

    private final MedicoRepositoryPort medicos;
    private final CitaRepositoryPort citas;
    private final CalendarioFestivosPort calendarioFestivos;
    private final Clock reloj;
    private final ZoneId zonaHoraria;

    public ConsultarDisponibilidadUseCase(MedicoRepositoryPort medicos, CitaRepositoryPort citas,
                                          CalendarioFestivosPort calendarioFestivos, Clock reloj,
                                          ZoneId zonaHoraria) {
        this.medicos = medicos;
        this.citas = citas;
        this.calendarioFestivos = calendarioFestivos;
        this.reloj = reloj;
        this.zonaHoraria = zonaHoraria;
    }

    public List<FranjaDisponibleDto> ejecutar(UUID medicoId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null || fechaInicio.isAfter(fechaFin)) {
            throw new ValidationException("RANGO_FECHAS_INVALIDO",
                    "fechaInicio y fechaFin son obligatorias y deben formar un rango valido");
        }
        medicos.buscarPorId(medicoId)
                .orElseThrow(() -> new NotFoundException("MEDICO_NO_ENCONTRADO", "El medico no existe"));

        Instant desde = fechaInicio.atStartOfDay(zonaHoraria).toInstant();
        Instant hasta = fechaFin.plusDays(1).atStartOfDay(zonaHoraria).toInstant();
        Set<Instant> ocupadas = citas.buscarFranjasOcupadas(medicoId, desde, hasta);
        Instant ahora = reloj.instant();
        List<FranjaDisponibleDto> resultado = new ArrayList<>();

        for (LocalDate fecha = fechaInicio; !fecha.isAfter(fechaFin); fecha = fecha.plusDays(1)) {
            if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY || calendarioFestivos.esFestivo(fecha)) {
                continue;
            }
            LocalTime cierre = fecha.getDayOfWeek() == DayOfWeek.SATURDAY ? CIERRE_SABADO : CIERRE_SEMANA;
            for (LocalTime hora = APERTURA; hora.isBefore(cierre); hora = hora.plusMinutes(30)) {
                Instant inicio = fecha.atTime(hora).atZone(zonaHoraria).toInstant();
                if (inicio.isAfter(ahora) && !ocupadas.contains(inicio)) {
                    resultado.add(new FranjaDisponibleDto(inicio, inicio.plusSeconds(30 * 60L)));
                }
            }
        }
        return List.copyOf(resultado);
    }
}
