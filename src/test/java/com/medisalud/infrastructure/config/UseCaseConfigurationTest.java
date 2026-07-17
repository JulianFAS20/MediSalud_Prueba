package com.medisalud.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class UseCaseConfigurationTest {

    private final UseCaseConfiguration configuration = new UseCaseConfiguration();

    @Test
    void debeUsarRelojFijoCuandoEstaConfigurado() {
        Instant instanteFijo = Instant.parse("2030-01-16T15:20:00Z");
        MedisaludProperties properties = new MedisaludProperties();
        properties.setRelojFijo(instanteFijo);

        Clock reloj = configuration.reloj(properties);

        assertThat(reloj.instant()).isEqualTo(instanteFijo);
        assertThat(reloj.getZone()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void debeUsarRelojDelSistemaCuandoNoHayInstanteFijo() {
        Instant antes = Instant.now();

        Clock reloj = configuration.reloj(new MedisaludProperties());

        assertThat(reloj.instant()).isBetween(antes, Instant.now());
        assertThat(reloj.getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
