package com.medisalud.domain.model;

public record Paginacion(int pagina, int tamanio) {

    public Paginacion {
        if (pagina < 0) {
            throw new IllegalArgumentException("El numero de pagina no puede ser negativo");
        }
        if (tamanio < 1) {
            throw new IllegalArgumentException("El tamanio de pagina debe ser mayor que cero");
        }
    }
}
