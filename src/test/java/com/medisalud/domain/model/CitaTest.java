package com.medisalud.domain.model;

import com.medisalud.domain.exception.ConflictException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CitaTest {

    private static final Instant INICIO = Instant.parse("2026-06-10T13:00:00Z");
    private static final Instant CANCELADA_EN = Instant.parse("2026-06-10T12:00:00Z");

    @Test
    void debeProgramarCitaConSusInvariantesIniciales() {
        UUID pacienteId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();

        Cita cita = Cita.programar(pacienteId, medicoId, new FranjaHoraria(INICIO));

        assertThat(cita.id()).isNotNull();
        assertThat(cita.pacienteId()).isEqualTo(pacienteId);
        assertThat(cita.medicoId()).isEqualTo(medicoId);
        assertThat(cita.estado()).isEqualTo(EstadoCita.PROGRAMADA);
        assertThat(cita.canceladaEn()).isNull();
        assertThat(cita.franja().fin()).isEqualTo(INICIO.plusSeconds(30 * 60L));
    }

    @Test
    void debeCancelarUnaCitaProgramada() {
        Cita cita = citaProgramada();

        cita.cancelar(CANCELADA_EN);

        assertThat(cita.estado()).isEqualTo(EstadoCita.CANCELADA);
        assertThat(cita.canceladaEn()).isEqualTo(CANCELADA_EN);
    }

    @Test
    void noDebePermitirCancelarDosVecesLaMismaCita() {
        Cita cita = citaProgramada();
        cita.cancelar(CANCELADA_EN);

        assertThatThrownBy(() -> cita.cancelar(CANCELADA_EN.plusSeconds(60)))
                .isInstanceOf(ConflictException.class)
                .extracting(error -> ((ConflictException) error).codigo())
                .isEqualTo("CITA_NO_PROGRAMADA");
    }

    @Test
    void noDebeRestaurarCitaCanceladaSinFechaDeCancelacion() {
        assertThatThrownBy(() -> Cita.restaurar(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new FranjaHoraria(INICIO), EstadoCita.CANCELADA, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("debe registrar");
    }

    @Test
    void noDebeRestaurarCitaProgramadaConFechaDeCancelacion() {
        assertThatThrownBy(() -> Cita.restaurar(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new FranjaHoraria(INICIO), EstadoCita.PROGRAMADA, CANCELADA_EN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solo una cita cancelada");
    }

    @Test
    void debeRestaurarUnaCitaCanceladaConsistente() {
        UUID citaId = UUID.randomUUID();

        Cita cita = Cita.restaurar(citaId, UUID.randomUUID(), UUID.randomUUID(),
                new FranjaHoraria(INICIO), EstadoCita.CANCELADA, CANCELADA_EN);

        assertThat(cita.id()).isEqualTo(citaId);
        assertThat(cita.estado()).isEqualTo(EstadoCita.CANCELADA);
        assertThat(cita.canceladaEn()).isEqualTo(CANCELADA_EN);
    }

    private Cita citaProgramada() {
        return Cita.programar(UUID.randomUUID(), UUID.randomUUID(), new FranjaHoraria(INICIO));
    }
}
