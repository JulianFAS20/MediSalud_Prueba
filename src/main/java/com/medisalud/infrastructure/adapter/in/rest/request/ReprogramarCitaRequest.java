package com.medisalud.infrastructure.adapter.in.rest.request;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ReprogramarCitaRequest(@NotNull OffsetDateTime nuevaFechaHora) {
}
