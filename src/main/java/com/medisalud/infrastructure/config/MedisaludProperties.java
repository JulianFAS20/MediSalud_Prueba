package com.medisalud.infrastructure.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "medisalud")
public class MedisaludProperties {

    private String zonaHoraria = "America/Bogota";
    private Instant relojFijo;
    private List<LocalDate> festivos = new ArrayList<>();
    @Min(1)
    private int maximoDiasDisponibilidad = 90;
    @Min(1)
    private int maximoTamanioPaginaCitas = 100;
}
