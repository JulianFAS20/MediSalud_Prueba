package com.medisalud.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JornadaAtencionTest {

    @Test
    void debeGenerarFranjasUsandoLaDuracionDelDominio() {
        JornadaAtencion jornada = new JornadaAtencion(LocalTime.of(8, 0), LocalTime.of(18, 0));

        assertThat(jornada.iniciosDeFranja())
                .hasSize(20)
                .first().isEqualTo(LocalTime.of(8, 0));
        assertThat(jornada.iniciosDeFranja())
                .last().isEqualTo(LocalTime.of(17, 30));
    }

    @Test
    void debeIncluirSoloFranjasCompletasDentroDeLaJornada() {
        JornadaAtencion jornada = new JornadaAtencion(LocalTime.of(8, 0), LocalTime.of(18, 0));

        assertThat(jornada.contiene(LocalTime.of(8, 0))).isTrue();
        assertThat(jornada.contiene(LocalTime.of(17, 30))).isTrue();
        assertThat(jornada.contiene(LocalTime.of(7, 30))).isFalse();
        assertThat(jornada.contiene(LocalTime.of(18, 0))).isFalse();
        assertThat(jornada.contiene(LocalTime.of(23, 30))).isFalse();
    }

    @Test
    void debeRechazarUnaJornadaSinOrdenCronologico() {
        assertThatThrownBy(() -> new JornadaAtencion(LocalTime.of(18, 0), LocalTime.of(8, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La apertura debe ser anterior al cierre");
    }

    @Test
    void debeRechazarUnaJornadaMasCortaQueUnaFranja() {
        assertThatThrownBy(() -> new JornadaAtencion(LocalTime.of(8, 0), LocalTime.of(8, 15)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La jornada debe permitir al menos una franja de atencion");
    }
}
