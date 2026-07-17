package com.medisalud.application.validation;

import com.medisalud.domain.exception.ValidationException;
import com.medisalud.domain.model.FranjaHoraria;
import com.medisalud.domain.model.JornadaAtencion;
import com.medisalud.domain.service.PoliticaHorarioAtencion;

import java.time.LocalTime;
import java.time.ZonedDateTime;

public final class HorarioLaboralReservaValidator implements ReservaValidator {

    private final PoliticaHorarioAtencion politicaHorario;

    public HorarioLaboralReservaValidator(PoliticaHorarioAtencion politicaHorario) {
        this.politicaHorario = politicaHorario;
    }

    @Override
    public void validar(ReservaValidationContext contexto) {
        if (!contexto.franja().inicio().isAfter(contexto.ahora())) {
            throw new ValidationException("CITA_NO_FUTURA", "La cita debe programarse en una fecha futura");
        }

        ZonedDateTime inicio = contexto.franja().inicio().atZone(contexto.zonaHoraria());
        LocalTime hora = inicio.toLocalTime();
        if (!politicaHorario.esInicioAlineado(hora)) {
            throw new ValidationException("FRANJA_INVALIDA",
                    "La cita debe iniciar en una franja exacta de "
                            + FranjaHoraria.DURACION.toMinutes() + " minutos");
        }

        JornadaAtencion jornada = politicaHorario.jornadaPara(inicio.toLocalDate())
                .orElseThrow(() -> new ValidationException(
                        "DIA_NO_LABORAL", "No hay atencion los domingos ni festivos"));
        if (!jornada.contiene(hora)) {
            throw new ValidationException("FUERA_DE_HORARIO",
                    "La franja esta fuera del horario laboral del medico");
        }
    }
}
