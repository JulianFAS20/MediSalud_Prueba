package com.medisalud.infrastructure.adapter.out.persistence;

import com.medisalud.domain.model.Paciente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pacientes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PacienteJpaEntity {

    @Id
    private UUID id;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Column(name = "documento_identidad", nullable = false, unique = true, length = 50)
    private String documentoIdentidad;

    @Column(nullable = false, length = 30)
    private String telefono;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private PacienteJpaEntity(Paciente paciente) {
        this.id = paciente.id();
        this.nombreCompleto = paciente.nombreCompleto();
        this.documentoIdentidad = paciente.documentoIdentidad();
        this.telefono = paciente.telefono();
        this.email = paciente.email();
        this.fechaNacimiento = paciente.fechaNacimiento();
    }

    public static PacienteJpaEntity desde(Paciente paciente) {
        return new PacienteJpaEntity(paciente);
    }

    public Paciente aDominio() {
        return Paciente.restaurar(id, nombreCompleto, documentoIdentidad, telefono, email, fechaNacimiento);
    }
}
