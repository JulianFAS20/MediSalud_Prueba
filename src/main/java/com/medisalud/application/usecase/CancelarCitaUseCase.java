package com.medisalud.application.usecase;

import com.medisalud.application.dto.CitaDto;
import com.medisalud.application.port.TransaccionPort;
import com.medisalud.domain.exception.NotFoundException;
import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.Penalizacion;
import com.medisalud.domain.port.CitaRepositoryPort;
import com.medisalud.domain.port.PenalizacionRepositoryPort;
import com.medisalud.domain.service.PenalizacionCancelacionStrategy;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public final class CancelarCitaUseCase {

    private final CitaRepositoryPort citas;
    private final PenalizacionRepositoryPort penalizaciones;
    private final PenalizacionCancelacionStrategy estrategiaPenalizacion;
    private final TransaccionPort transacciones;
    private final Clock reloj;

    public CancelarCitaUseCase(CitaRepositoryPort citas, PenalizacionRepositoryPort penalizaciones,
                               PenalizacionCancelacionStrategy estrategiaPenalizacion,
                               TransaccionPort transacciones, Clock reloj) {
        this.citas = citas;
        this.penalizaciones = penalizaciones;
        this.estrategiaPenalizacion = estrategiaPenalizacion;
        this.transacciones = transacciones;
        this.reloj = reloj;
    }

    public CitaDto ejecutar(UUID citaId) {
        return transacciones.ejecutar(() -> cancelar(citaId, reloj.instant()));
    }

    CitaDto cancelar(UUID citaId, Instant ahora) {
        Cita cita = citas.buscarPorId(citaId)
                .orElseThrow(() -> new NotFoundException("CITA_NO_ENCONTRADA", "La cita no existe"));
        boolean debePenalizar = estrategiaPenalizacion.debePenalizar(cita, ahora);
        cita.cancelar(ahora);
        Cita citaGuardada = citas.guardar(cita);
        if (debePenalizar) {
            penalizaciones.guardar(Penalizacion.porCancelacionTardia(cita.pacienteId(), cita.id(), ahora));
        }
        return CitaDto.desde(citaGuardada);
    }
}
