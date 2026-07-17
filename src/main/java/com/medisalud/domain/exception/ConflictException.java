package com.medisalud.domain.exception;

public final class ConflictException extends DomainException {

    public ConflictException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
