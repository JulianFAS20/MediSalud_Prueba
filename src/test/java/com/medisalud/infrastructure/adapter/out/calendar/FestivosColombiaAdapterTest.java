package com.medisalud.infrastructure.adapter.out.calendar;

import com.medisalud.infrastructure.config.MedisaludProperties;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FestivosColombiaAdapterTest {

    @Test
    void debeCalcularFestivosFijosEmilianiPascuaYNuevaLey() {
        MedisaludProperties properties = new MedisaludProperties();
        properties.setFestivos(List.of(LocalDate.of(2026, 9, 10)));
        var calendario = new FestivosColombiaAdapter(properties);

        assertThat(calendario.esFestivo(LocalDate.of(2026, 1, 1))).isTrue();
        assertThat(calendario.esFestivo(LocalDate.of(2026, 1, 12))).isTrue();
        assertThat(calendario.esFestivo(LocalDate.of(2026, 4, 2))).isTrue();
        assertThat(calendario.esFestivo(LocalDate.of(2026, 4, 3))).isTrue();
        assertThat(calendario.esFestivo(LocalDate.of(2026, 7, 13))).isTrue();
        assertThat(calendario.esFestivo(LocalDate.of(2026, 9, 10))).isTrue();
        assertThat(calendario.esFestivo(LocalDate.of(2026, 9, 11))).isFalse();
    }
}
