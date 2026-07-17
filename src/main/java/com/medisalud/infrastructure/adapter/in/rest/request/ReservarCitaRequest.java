package com.medisalud.infrastructure.adapter.in.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Datos para reservar una cita")
public record ReservarCitaRequest(
        @Schema(example = "6b3e3b0c-6296-4468-8163-2cf66882d81b") @NotNull UUID pacienteId,
        @Schema(example = "7dfc0f5a-2173-4cb0-97a5-8fb82db1e62c") @NotNull UUID medicoId,
        @Schema(example = "2027-02-01T08:30:00-05:00") @NotNull OffsetDateTime fechaHora) {
}
