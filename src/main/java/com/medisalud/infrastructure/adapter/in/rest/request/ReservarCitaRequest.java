package com.medisalud.infrastructure.adapter.in.rest.request;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservarCitaRequest(
        @NotNull UUID pacienteId,
        @NotNull UUID medicoId,
        @NotNull OffsetDateTime fechaHora) {
}
