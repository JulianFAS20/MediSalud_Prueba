package com.medisalud.infrastructure.adapter.in.rest.response;

import com.medisalud.application.dto.CitaDto;
import com.medisalud.domain.model.Pagina;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Schema(description = "Pagina de citas que coincide con los filtros")
public record PaginaCitasResponse(
        List<CitaResponse> contenido,
        @Schema(example = "0") int pagina,
        @Schema(example = "20") int tamanio,
        @Schema(example = "34") long totalElementos,
        @Schema(example = "2") int totalPaginas,
        @Schema(example = "true") boolean primera,
        @Schema(example = "false") boolean ultima) {

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
