package com.medisalud.domain.model;

import com.medisalud.domain.exception.ValidationException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Paciente {

    private final UUID id;
    private final String nombreCompleto;
    private final String documentoIdentidad;
    private final String telefono;
    private final String email;
    private final LocalDate fechaNacimiento;

    private Paciente(UUID id, String nombreCompleto, String documentoIdentidad, String telefono,
                     String email, LocalDate fechaNacimiento) {
        this.id = Objects.requireNonNull(id, "El id del paciente es obligatorio");
        this.nombreCompleto = ValidacionesDominio.textoObligatorio(nombreCompleto, "El nombre completo", 3, 100);
        this.documentoIdentidad = ValidacionesDominio.textoObligatorio(
                documentoIdentidad, "El documento de identidad", 7, 50);
        this.telefono = ValidacionesDominio.telefono(telefono, true);
        this.email = ValidacionesDominio.email(email, true);
        this.fechaNacimiento = fechaNacimiento;
    }

    public static Paciente registrar(String nombreCompleto, String documentoIdentidad, String telefono,
                                     String email, LocalDate fechaNacimiento) {
        return new Paciente(UUID.randomUUID(), nombreCompleto, documentoIdentidad, telefono, email, fechaNacimiento);
    }

    public static Paciente restaurar(UUID id, String nombreCompleto, String documentoIdentidad, String telefono,
                                     String email, LocalDate fechaNacimiento) {
        return new Paciente(id, nombreCompleto, documentoIdentidad, telefono, email, fechaNacimiento);
    }

    public void validarFechaNacimiento(LocalDate fechaActual) {
        Objects.requireNonNull(fechaActual, "La fecha actual es obligatoria");
        if (fechaNacimiento != null && fechaNacimiento.isAfter(fechaActual)) {
            throw new ValidationException("FECHA_NACIMIENTO_FUTURA",
                    "No se puede agendar para un paciente con fecha de nacimiento futura");
        }
    }

    public UUID id() {
        return id;
    }

    public String nombreCompleto() {
        return nombreCompleto;
    }

    public String documentoIdentidad() {
        return documentoIdentidad;
    }

    public String telefono() {
        return telefono;
    }

    public String email() {
        return email;
    }

    public LocalDate fechaNacimiento() {
        return fechaNacimiento;
    }
}
