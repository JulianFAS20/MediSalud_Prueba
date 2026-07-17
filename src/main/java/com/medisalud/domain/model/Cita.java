package com.medisalud.domain.model;

import com.medisalud.domain.exception.ConflictException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Cita {

    private final UUID id;
    private final UUID pacienteId;
    private final UUID medicoId;
    private final FranjaHoraria franja;
    private EstadoCita estado;
    private Instant canceladaEn;

    private Cita(UUID id, UUID pacienteId, UUID medicoId, FranjaHoraria franja,
                 EstadoCita estado, Instant canceladaEn) {
        this.id = Objects.requireNonNull(id, "El id de la cita es obligatorio");
        this.pacienteId = Objects.requireNonNull(pacienteId, "El paciente es obligatorio");
        this.medicoId = Objects.requireNonNull(medicoId, "El medico es obligatorio");
        this.franja = Objects.requireNonNull(franja, "La franja horaria es obligatoria");
        this.estado = Objects.requireNonNull(estado, "El estado es obligatorio");
        this.canceladaEn = canceladaEn;
        validarInvariantes();
    }

    public static Cita programar(UUID pacienteId, UUID medicoId, FranjaHoraria franja) {
        return new Cita(UUID.randomUUID(), pacienteId, medicoId, franja, EstadoCita.PROGRAMADA, null);
    }

    public static Cita restaurar(UUID id, UUID pacienteId, UUID medicoId, FranjaHoraria franja,
                                 EstadoCita estado, Instant canceladaEn) {
        return new Cita(id, pacienteId, medicoId, franja, estado, canceladaEn);
    }

    public void cancelar(Instant fechaCancelacion) {
        if (estado != EstadoCita.PROGRAMADA) {
            throw new ConflictException("CITA_NO_PROGRAMADA", "Solo una cita programada puede cancelarse");
        }
        this.estado = EstadoCita.CANCELADA;
        this.canceladaEn = Objects.requireNonNull(fechaCancelacion, "La fecha de cancelacion es obligatoria");
    }

    private void validarInvariantes() {
        if (estado == EstadoCita.CANCELADA && canceladaEn == null) {
            throw new IllegalArgumentException("Una cita cancelada debe registrar su fecha de cancelacion");
        }
        if (estado != EstadoCita.CANCELADA && canceladaEn != null) {
            throw new IllegalArgumentException("Solo una cita cancelada puede tener fecha de cancelacion");
        }
    }

    public UUID id() {
        return id;
    }

    public UUID pacienteId() {
        return pacienteId;
    }

    public UUID medicoId() {
        return medicoId;
    }

    public FranjaHoraria franja() {
        return franja;
    }

    public EstadoCita estado() {
        return estado;
    }

    public Instant canceladaEn() {
        return canceladaEn;
    }
}
