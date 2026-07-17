package com.medisalud.application.usecase;

import com.medisalud.application.dto.CitaDto;
import com.medisalud.domain.exception.ValidationException;
import com.medisalud.domain.model.EstadoCita;
import com.medisalud.domain.model.FiltroCitas;
import com.medisalud.domain.model.Pagina;
import com.medisalud.domain.model.Paginacion;
import com.medisalud.domain.port.CitaRepositoryPort;

import java.time.Instant;
import java.util.UUID;

public final class ListarCitasUseCase {

    private static final int TAMANIO_PAGINA_POR_DEFECTO = 20;

    private final CitaRepositoryPort citas;
    private final int tamanioMaximoPagina;

    public ListarCitasUseCase(CitaRepositoryPort citas, int tamanioMaximoPagina) {
        this.citas = citas;
        this.tamanioMaximoPagina = tamanioMaximoPagina;
    }

    public Pagina<CitaDto> ejecutar(UUID medicoId, UUID pacienteId, EstadoCita estado,
                                    Instant fechaInicio, Instant fechaFin, int pagina, Integer tamanioSolicitado) {
        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            throw new ValidationException("RANGO_FECHAS_INVALIDO", "fechaInicio no puede ser posterior a fechaFin");
        }
        if (pagina < 0) {
            throw new ValidationException("PAGINA_INVALIDA", "page no puede ser negativo");
        }
        int tamanio = tamanioSolicitado == null
                ? Math.min(TAMANIO_PAGINA_POR_DEFECTO, tamanioMaximoPagina)
                : tamanioSolicitado;
        if (tamanio < 1 || tamanio > tamanioMaximoPagina) {
            throw new ValidationException("TAMANIO_PAGINA_INVALIDO",
                    "size debe estar entre 1 y " + tamanioMaximoPagina);
        }
        return citas.buscar(
                        new FiltroCitas(medicoId, pacienteId, estado, fechaInicio, fechaFin),
                        new Paginacion(pagina, tamanio))
                .map(CitaDto::desde);
    }
}
