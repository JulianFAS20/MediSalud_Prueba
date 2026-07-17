package com.medisalud.infrastructure.adapter.in.rest.response;

import com.medisalud.application.dto.CitaDto;
import com.medisalud.domain.model.Pagina;

import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

public record PaginaCitasResponse(
        List<CitaResponse> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas,
        boolean primera,
        boolean ultima) {

    public PaginaCitasResponse {
        contenido = List.copyOf(Objects.requireNonNull(contenido, "El contenido es obligatorio"));
    }

    public static PaginaCitasResponse desde(Pagina<CitaDto> resultado, ZoneId zonaHoraria) {
        Objects.requireNonNull(resultado, "El resultado paginado es obligatorio");
        Objects.requireNonNull(zonaHoraria, "La zona horaria es obligatoria");
        return new PaginaCitasResponse(
                resultado.contenido().stream()
                        .map(cita -> CitaResponse.desde(cita, zonaHoraria))
                        .toList(),
                resultado.pagina(),
                resultado.tamanio(),
                resultado.totalElementos(),
                resultado.totalPaginas(),
                resultado.primera(),
                resultado.ultima());
    }
}
