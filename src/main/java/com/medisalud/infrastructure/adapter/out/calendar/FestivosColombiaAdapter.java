package com.medisalud.infrastructure.adapter.out.calendar;

import com.medisalud.domain.port.CalendarioFestivosPort;
import com.medisalud.infrastructure.config.MedisaludProperties;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class FestivosColombiaAdapter implements CalendarioFestivosPort {

    private static final int VIGENCIA_FESTIVO_CHIQUINQUIRA = 2026;

    private final Set<LocalDate> festivosAdicionales;
    private final ConcurrentMap<Integer, Set<LocalDate>> cachePorAnio = new ConcurrentHashMap<>();

    public FestivosColombiaAdapter(MedisaludProperties properties) {
        this.festivosAdicionales = Set.copyOf(properties.getFestivos());
    }

    @Override
    public boolean esFestivo(LocalDate fecha) {
        return festivosAdicionales.contains(fecha)
                || cachePorAnio.computeIfAbsent(fecha.getYear(), this::calcularFestivos).contains(fecha);
    }

    private Set<LocalDate> calcularFestivos(int anio) {
        Set<LocalDate> festivos = new HashSet<>();

        festivos.add(LocalDate.of(anio, Month.JANUARY, 1));
        festivos.add(LocalDate.of(anio, Month.MAY, 1));
        festivos.add(LocalDate.of(anio, Month.JULY, 20));
        festivos.add(LocalDate.of(anio, Month.AUGUST, 7));
        festivos.add(LocalDate.of(anio, Month.DECEMBER, 8));
        festivos.add(LocalDate.of(anio, Month.DECEMBER, 25));

        festivos.add(trasladarAlLunes(LocalDate.of(anio, Month.JANUARY, 6)));
        festivos.add(trasladarAlLunes(LocalDate.of(anio, Month.MARCH, 19)));
        festivos.add(trasladarAlLunes(LocalDate.of(anio, Month.JUNE, 29)));
        festivos.add(trasladarAlLunes(LocalDate.of(anio, Month.AUGUST, 15)));
        festivos.add(trasladarAlLunes(LocalDate.of(anio, Month.OCTOBER, 12)));
        festivos.add(trasladarAlLunes(LocalDate.of(anio, Month.NOVEMBER, 1)));
        festivos.add(trasladarAlLunes(LocalDate.of(anio, Month.NOVEMBER, 11)));

        LocalDate pascua = calcularDomingoPascua(anio);
        festivos.add(pascua.minusDays(3));
        festivos.add(pascua.minusDays(2));
        festivos.add(pascua.plusDays(43));
        festivos.add(pascua.plusDays(64));
        festivos.add(pascua.plusDays(71));

        if (anio >= VIGENCIA_FESTIVO_CHIQUINQUIRA) {
            festivos.add(trasladarAlLunes(LocalDate.of(anio, Month.JULY, 9)));
        }
        return Set.copyOf(festivos);
    }

    private LocalDate trasladarAlLunes(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    // Algoritmo gregoriano de Meeus/Jones/Butcher.
    private LocalDate calcularDomingoPascua(int anio) {
        int a = anio % 19;
        int b = anio / 100;
        int c = anio % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int baseFechaPascua = h + l - 7 * m + 114;
        int mes = baseFechaPascua / 31;
        int dia = (baseFechaPascua % 31) + 1;
        return LocalDate.of(anio, mes, dia);
    }
}
