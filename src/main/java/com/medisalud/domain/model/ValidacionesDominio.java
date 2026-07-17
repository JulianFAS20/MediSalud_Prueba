package com.medisalud.domain.model;

import com.medisalud.domain.exception.ValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

final class ValidacionesDominio {

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);

    private ValidacionesDominio() {
    }

    static String textoObligatorio(String valor, String campo, int minimo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidationException("CAMPO_OBLIGATORIO", campo + " es obligatorio");
        }
        String normalizado = valor.trim();
        if (normalizado.length() < minimo || normalizado.length() > maximo) {
            throw new ValidationException("LONGITUD_INVALIDA",
                    campo + " debe tener entre " + minimo + " y " + maximo + " caracteres");
        }
        return normalizado;
    }

    static String email(String valor, boolean obligatorio) {
        if (valor == null || valor.isBlank()) {
            if (obligatorio) {
                throw new ValidationException("EMAIL_OBLIGATORIO", "El email es obligatorio");
            }
            return null;
        }
        String normalizado = valor.trim().toLowerCase(Locale.ROOT);
        if (normalizado.length() > 254 || !EMAIL.matcher(normalizado).matches()) {
            throw new ValidationException("EMAIL_INVALIDO", "El email no tiene un formato valido");
        }
        return normalizado;
    }

    static String telefono(String valor, boolean obligatorio) {
        if (valor == null || valor.isBlank()) {
            if (obligatorio) {
                throw new ValidationException("TELEFONO_OBLIGATORIO", "El telefono es obligatorio");
            }
            return null;
        }
        String normalizado = valor.trim();
        long digitos = normalizado.chars().filter(Character::isDigit).count();
        if (digitos < 7) {
            throw new ValidationException("TELEFONO_INVALIDO", "El telefono debe contener al menos 7 digitos");
        }
        return normalizado;
    }
}
