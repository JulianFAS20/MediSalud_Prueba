package com.medisalud.domain.exception;

public final class NotFoundException extends DomainException {

    public NotFoundException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
