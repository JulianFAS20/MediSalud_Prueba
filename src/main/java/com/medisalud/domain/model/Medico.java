package com.medisalud.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class Medico {

    private final UUID id;
    private final String nombreCompleto;
    private final String especialidad;
    private final String telefono;
    private final String email;

    private Medico(UUID id, String nombreCompleto, String especialidad, String telefono, String email) {
        this.id = Objects.requireNonNull(id, "El id del medico es obligatorio");
        this.nombreCompleto = ValidacionesDominio.textoObligatorio(nombreCompleto, "El nombre completo", 3, 100);
        this.especialidad = ValidacionesDominio.textoObligatorio(especialidad, "La especialidad", 2, 100);
        this.telefono = ValidacionesDominio.telefono(telefono, false);
        this.email = ValidacionesDominio.email(email, false);
    }

    public static Medico registrar(String nombreCompleto, String especialidad, String telefono, String email) {
        return new Medico(UUID.randomUUID(), nombreCompleto, especialidad, telefono, email);
    }

    public static Medico restaurar(UUID id, String nombreCompleto, String especialidad, String telefono, String email) {
        return new Medico(id, nombreCompleto, especialidad, telefono, email);
    }

    public UUID id() {
        return id;
    }

    public String nombreCompleto() {
        return nombreCompleto;
    }

    public String especialidad() {
        return especialidad;
    }

    public String telefono() {
        return telefono;
    }

    public String email() {
        return email;
    }
}
