package com.medisalud.domain.exception;

public abstract class DomainException extends RuntimeException {

    private final String codigo;

    protected DomainException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
