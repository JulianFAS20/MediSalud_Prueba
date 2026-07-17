package com.medisalud.domain.exception;

public final class ValidationException extends DomainException {

    public ValidationException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
