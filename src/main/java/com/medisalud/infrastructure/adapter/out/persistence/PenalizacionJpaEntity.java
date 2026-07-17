package com.medisalud.infrastructure.adapter.out.persistence;

import com.medisalud.domain.model.Penalizacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "penalizaciones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PenalizacionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "cita_id", nullable = false, unique = true)
    private UUID citaId;

    @Column(name = "registrada_en", nullable = false)
    private Instant registradaEn;

    @Column(nullable = false, length = 200)
    private String motivo;

    private PenalizacionJpaEntity(Penalizacion penalizacion) {
        this.id = penalizacion.id();
        this.pacienteId = penalizacion.pacienteId();
        this.citaId = penalizacion.citaId();
        this.registradaEn = penalizacion.registradaEn();
        this.motivo = penalizacion.motivo();
    }

    public static PenalizacionJpaEntity desde(Penalizacion penalizacion) {
        return new PenalizacionJpaEntity(penalizacion);
    }

    public Penalizacion aDominio() {
        return new Penalizacion(id, pacienteId, citaId, registradaEn, motivo);
    }
}
