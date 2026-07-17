package com.medisalud.infrastructure.adapter.out.persistence;

import com.medisalud.MedisaludApplication;
import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.FranjaHoraria;
import com.medisalud.domain.model.Medico;
import com.medisalud.domain.model.Paciente;
import com.medisalud.domain.port.CitaRepositoryPort;
import com.medisalud.domain.port.MedicoRepositoryPort;
import com.medisalud.domain.port.PacienteRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = MedisaludApplication.class)
@AutoConfigureMockMvc
class CitaPersistenceConcurrencyIntegrationTest {

    @Autowired
    private CitaRepositoryPort citas;

    @Autowired
    private PacienteRepositoryPort pacientes;

    @Autowired
    private MedicoRepositoryPort medicos;

    @Test
    void baseDeDatosDebeImpedirDosCitasActivasDelMismoMedicoEnLaMismaFranja() {
        Medico medico = guardarMedico("Doctor Restriccion Medico");
        Paciente primerPaciente = guardarPaciente("DOC-CONC-M-1");
        Paciente segundoPaciente = guardarPaciente("DOC-CONC-M-2");
        FranjaHoraria franja = new FranjaHoraria(Instant.parse("2031-01-15T13:00:00Z"));
        citas.guardar(Cita.programar(primerPaciente.id(), medico.id(), franja));

        assertThatThrownBy(() ->
                citas.guardar(Cita.programar(segundoPaciente.id(), medico.id(), franja)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void baseDeDatosDebeImpedirDosCitasActivasDelMismoPacienteEnLaMismaFranja() {
        Paciente paciente = guardarPaciente("DOC-CONC-P-1");
        Medico primerMedico = guardarMedico("Doctor Restriccion Paciente Uno");
        Medico segundoMedico = guardarMedico("Doctor Restriccion Paciente Dos");
        FranjaHoraria franja = new FranjaHoraria(Instant.parse("2031-01-16T13:00:00Z"));
        citas.guardar(Cita.programar(paciente.id(), primerMedico.id(), franja));

        assertThatThrownBy(() ->
                citas.guardar(Cita.programar(paciente.id(), segundoMedico.id(), franja)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Paciente guardarPaciente(String documento) {
        return pacientes.guardar(Paciente.registrar(
                "Paciente Concurrencia", documento, "3001234567",
                documento.toLowerCase() + "@example.com", null));
    }

    private Medico guardarMedico(String nombre) {
        return medicos.guardar(Medico.registrar(nombre, "Medicina General", null, null));
    }
}
