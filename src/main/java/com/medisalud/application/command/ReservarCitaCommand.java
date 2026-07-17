package com.medisalud.application.command;

import java.time.Instant;
import java.util.UUID;

public record ReservarCitaCommand(UUID pacienteId, UUID medicoId, Instant fechaHora) {
}
