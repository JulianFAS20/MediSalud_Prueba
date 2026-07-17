package com.medisalud.infrastructure.adapter.out.persistence;

import com.medisalud.domain.model.Medico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "medicos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Column(nullable = false, length = 100)
    private String especialidad;

    @Column(length = 30)
    private String telefono;

    @Column(length = 254)
    private String email;

    private MedicoJpaEntity(Medico medico) {
        this.id = medico.id();
        this.nombreCompleto = medico.nombreCompleto();
        this.especialidad = medico.especialidad();
        this.telefono = medico.telefono();
        this.email = medico.email();
    }

    public static MedicoJpaEntity desde(Medico medico) {
        return new MedicoJpaEntity(medico);
    }

    public Medico aDominio() {
        return Medico.restaurar(id, nombreCompleto, especialidad, telefono, email);
    }
}
