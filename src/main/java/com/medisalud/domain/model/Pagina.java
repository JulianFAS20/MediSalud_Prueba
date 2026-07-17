package com.medisalud.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record Pagina<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas) {

    public Pagina {
        contenido = List.copyOf(Objects.requireNonNull(contenido, "El contenido es obligatorio"));
        if (pagina < 0 || tamanio < 1 || totalElementos < 0 || totalPaginas < 0) {
            throw new IllegalArgumentException("Los metadatos de paginacion no son validos");
        }
    }

    public boolean primera() {
        return pagina == 0;
    }

    public boolean ultima() {
        return totalPaginas == 0 || pagina >= totalPaginas - 1;
    }

    public <R> Pagina<R> map(Function<T, R> mapper) {
        Objects.requireNonNull(mapper, "La funcion de mapeo es obligatoria");
        return new Pagina<>(contenido.stream().map(mapper).toList(),
                pagina, tamanio, totalElementos, totalPaginas);
    }
}
