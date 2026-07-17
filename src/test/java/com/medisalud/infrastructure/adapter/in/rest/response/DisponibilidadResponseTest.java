package com.medisalud.infrastructure.adapter.in.rest.response;

import com.medisalud.application.dto.FranjaDisponibleDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisponibilidadResponseTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @Test
    void debeCalcularLaCantidadYConvertirLasFranjasALaZonaConfigurada() {
        var primera = new FranjaDisponibleDto(
                Instant.parse("2026-06-10T13:00:00Z"), Instant.parse("2026-06-10T13:30:00Z"));
        var segunda = new FranjaDisponibleDto(
                Instant.parse("2026-06-10T13:30:00Z"), Instant.parse("2026-06-10T14:00:00Z"));

        DisponibilidadResponse response = DisponibilidadResponse.desde(List.of(primera, segunda), ZONA);

        assertThat(response.cantidadFranjasDisponibles()).isEqualTo(2);
        assertThat(response.franjasDisponibles()).hasSize(2);
        assertThat(response.franjasDisponibles().getFirst().inicio().toString())
                .isEqualTo("2026-06-10T08:00-05:00");
        assertThatThrownBy(() -> response.franjasDisponibles().add(
                response.franjasDisponibles().getFirst()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void debeRepresentarDisponibilidadVaciaDeFormaExplicita() {
        DisponibilidadResponse response = DisponibilidadResponse.desde(List.of(), ZONA);

        assertThat(response.cantidadFranjasDisponibles()).isZero();
        assertThat(response.franjasDisponibles()).isEmpty();
    }

    @Test
    void debeImpedirConstruirUnaCantidadInconsistente() {
        assertThatThrownBy(() -> new DisponibilidadResponse(1, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("coincidir");
    }
}
