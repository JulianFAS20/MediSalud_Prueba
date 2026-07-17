package com.medisalud.infrastructure.adapter.out.persistence;

import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.EstadoCita;
import com.medisalud.domain.model.FranjaHoraria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "citas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CitaJpaEntity {

    @Id
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "medico_id", nullable = false)
    private UUID medicoId;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    @Column(name = "cancelada_en")
    private Instant canceladaEn;

    @Column(name = "clave_franja_medico", unique = true, length = 100)
    private String claveFranjaMedico;

    @Column(name = "clave_franja_paciente", unique = true, length = 100)
    private String claveFranjaPaciente;

    @SuppressWarnings("unused") // Hibernate asigna e incrementa la version mediante acceso a campo.
    @Version
    @Column(nullable = false)
    private Long version;

    private CitaJpaEntity(Cita cita) {
        this.id = cita.id();
        actualizarDesde(cita);
    }

    public static CitaJpaEntity desde(Cita cita) {
        return new CitaJpaEntity(cita);
    }

    public void actualizarDesde(Cita cita) {
        this.pacienteId = cita.pacienteId();
        this.medicoId = cita.medicoId();
        this.fechaHora = cita.franja().inicio();
        this.estado = cita.estado();
        this.canceladaEn = cita.canceladaEn();
        boolean programada = cita.estado() == EstadoCita.PROGRAMADA;
        this.claveFranjaMedico = programada ? medicoId + "|" + fechaHora : null;
        this.claveFranjaPaciente = programada ? pacienteId + "|" + fechaHora : null;
    }

    public Cita aDominio() {
        return Cita.restaurar(id, pacienteId, medicoId, new FranjaHoraria(fechaHora), estado, canceladaEn);
    }
}
