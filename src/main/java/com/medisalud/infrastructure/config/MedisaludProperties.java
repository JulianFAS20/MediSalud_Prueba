package com.medisalud.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "medisalud")
public class MedisaludProperties {

    private String zonaHoraria = "America/Bogota";
    private Instant relojFijo;
    private List<LocalDate> festivos = new ArrayList<>();
}
