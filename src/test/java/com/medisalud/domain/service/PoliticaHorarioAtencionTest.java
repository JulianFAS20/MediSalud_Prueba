package com.medisalud.domain.service;

import com.medisalud.domain.model.JornadaAtencion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class PoliticaHorarioAtencionTest {

    private static final LocalDate FESTIVO = LocalDate.of(2026, 6, 15);
    private final PoliticaHorarioAtencion politica = new PoliticaHorarioAtencion(FESTIVO::equals);

    @Test
    void debeDefinirLaJornadaDeLunesAViernes() {
        LocalDate miercoles = LocalDate.of(2026, 6, 10);

        assertThat(politica.jornadaPara(miercoles))
                .contains(new JornadaAtencion(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        assertThat(politica.iniciosDeFranja(miercoles)).hasSize(20);
    }

    @Test
    void debeDefinirUnaJornadaReducidaElSabado() {
        LocalDate sabado = LocalDate.of(2026, 6, 13);

        assertThat(politica.jornadaPara(sabado))
                .contains(new JornadaAtencion(LocalTime.of(8, 0), LocalTime.of(13, 0)));
        assertThat(politica.iniciosDeFranja(sabado)).hasSize(10);
    }

    @Test
    void noDebeOfrecerJornadaEnDomingoNiFestivo() {
        LocalDate domingo = LocalDate.of(2026, 6, 14);

        assertThat(politica.jornadaPara(domingo)).isEmpty();
        assertThat(politica.iniciosDeFranja(FESTIVO)).isEmpty();
    }

    @Test
    void debeValidarLaAlineacionConLaDuracionDeLaFranja() {
        assertThat(politica.esInicioAlineado(LocalTime.of(8, 0))).isTrue();
        assertThat(politica.esInicioAlineado(LocalTime.of(8, 30))).isTrue();
        assertThat(politica.esInicioAlineado(LocalTime.of(7, 30))).isTrue();
        assertThat(politica.esInicioAlineado(LocalTime.of(8, 15))).isFalse();
        assertThat(politica.esInicioAlineado(LocalTime.of(8, 0, 1))).isFalse();
    }
}
