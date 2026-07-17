package com.medisalud.domain.port;

import java.time.LocalDate;

public interface CalendarioFestivosPort {

    boolean esFestivo(LocalDate fecha);
}
