package com.medisalud.application.validation;

import com.medisalud.domain.exception.ValidationException;
import com.medisalud.domain.port.CalendarioFestivosPort;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public final class HorarioLaboralReservaValidator implements ReservaValidator {

    private static final LocalTime APERTURA = LocalTime.of(8, 0);
    private static final LocalTime CIERRE_SEMANA = LocalTime.of(18, 0);
    private static final LocalTime CIERRE_SABADO = LocalTime.of(13, 0);

    private final CalendarioFestivosPort calendarioFestivos;

    public HorarioLaboralReservaValidator(CalendarioFestivosPort calendarioFestivos) {
        this.calendarioFestivos = calendarioFestivos;
    }

    @Override
    public void validar(ReservaValidationContext contexto) {
        if (!contexto.franja().inicio().isAfter(contexto.ahora())) {
            throw new ValidationException("CITA_NO_FUTURA", "La cita debe programarse en una fecha futura");
        }

        ZonedDateTime inicio = contexto.franja().inicio().atZone(contexto.zonaHoraria());
        LocalTime hora = inicio.toLocalTime();
        if (hora.getSecond() != 0 || hora.getNano() != 0 || (hora.getMinute() != 0 && hora.getMinute() != 30)) {
            throw new ValidationException("FRANJA_INVALIDA",
                    "La cita debe iniciar en una franja exacta de 30 minutos");
        }

        DayOfWeek dia = inicio.getDayOfWeek();
        if (dia == DayOfWeek.SUNDAY || calendarioFestivos.esFestivo(inicio.toLocalDate())) {
            throw new ValidationException("DIA_NO_LABORAL", "No hay atencion los domingos ni festivos");
        }

        LocalTime cierre = dia == DayOfWeek.SATURDAY ? CIERRE_SABADO : CIERRE_SEMANA;
        LocalTime fin = hora.plusMinutes(30);
        if (hora.isBefore(APERTURA) || fin.isAfter(cierre)) {
            throw new ValidationException("FUERA_DE_HORARIO",
                    "La franja esta fuera del horario laboral del medico");
        }
    }
}
