package com.medisalud.domain.service;

import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.FranjaHoraria;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PenalizacionCancelacionTardiaStrategyTest {

    private static final Instant FECHA_CITA = Instant.parse("2026-06-10T15:00:00Z");
    private final PenalizacionCancelacionTardiaStrategy strategy = new PenalizacionCancelacionTardiaStrategy();
    private final Cita cita = Cita.programar(
            UUID.randomUUID(), UUID.randomUUID(), new FranjaHoraria(FECHA_CITA));

    @Test
    void debePenalizarCuandoFaltaMenosDeDosHoras() {
        assertThat(strategy.debePenalizar(cita, FECHA_CITA.minusSeconds(2 * 60 * 60L - 1))).isTrue();
    }

    @Test
    void noDebePenalizarEnElLimiteExactoDeDosHoras() {
        assertThat(strategy.debePenalizar(cita, FECHA_CITA.minusSeconds(2 * 60 * 60L))).isFalse();
    }

    @Test
    void noDebePenalizarCuandoHayMasDeDosHorasDeAntelacion() {
        assertThat(strategy.debePenalizar(cita, FECHA_CITA.minusSeconds(3 * 60 * 60L))).isFalse();
    }
}
