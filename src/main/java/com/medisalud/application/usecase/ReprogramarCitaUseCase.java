package com.medisalud.application.usecase;

import com.medisalud.application.command.ReservarCitaCommand;
import com.medisalud.application.dto.CitaDto;
import com.medisalud.application.port.TransaccionPort;
import com.medisalud.domain.exception.NotFoundException;
import com.medisalud.domain.exception.ValidationException;
import com.medisalud.domain.model.Cita;
import com.medisalud.domain.port.CitaRepositoryPort;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public final class ReprogramarCitaUseCase {

    private final CitaRepositoryPort citas;
    private final CancelarCitaUseCase cancelarCita;
    private final ReservarCitaUseCase reservarCita;
    private final TransaccionPort transacciones;
    private final Clock reloj;

    public ReprogramarCitaUseCase(CitaRepositoryPort citas, CancelarCitaUseCase cancelarCita,
                                  ReservarCitaUseCase reservarCita, TransaccionPort transacciones, Clock reloj) {
        this.citas = citas;
        this.cancelarCita = cancelarCita;
        this.reservarCita = reservarCita;
        this.transacciones = transacciones;
        this.reloj = reloj;
    }

    public CitaDto ejecutar(UUID citaId, Instant nuevaFechaHora) {
        return transacciones.ejecutar(() -> {
            Instant ahora = reloj.instant();
            Cita anterior = citas.buscarPorId(citaId)
                    .orElseThrow(() -> new NotFoundException("CITA_NO_ENCONTRADA", "La cita no existe"));
            if (anterior.franja().inicio().equals(nuevaFechaHora)) {
                throw new ValidationException("MISMA_FRANJA", "La nueva franja debe ser diferente a la actual");
            }

            cancelarCita.cancelar(citaId, ahora);
            return reservarCita.reservar(
                    new ReservarCitaCommand(anterior.pacienteId(), anterior.medicoId(), nuevaFechaHora), ahora);
        });
    }
}
