package com.medisalud.application.validation;

import com.medisalud.domain.exception.ConflictException;
import com.medisalud.domain.model.FranjaHoraria;
import com.medisalud.domain.model.Medico;
import com.medisalud.domain.model.Paciente;
import com.medisalud.domain.port.CitaRepositoryPort;
import com.medisalud.domain.port.PenalizacionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaValidatorsTest {

    private static final Instant AHORA = Instant.parse("2026-06-10T12:00:00Z");
    private static final Instant FECHA_CITA = Instant.parse("2026-06-10T14:00:00Z");

    @Mock
    private CitaRepositoryPort citas;
    @Mock
    private PenalizacionRepositoryPort penalizaciones;

    private Paciente paciente;
    private ReservaValidationContext contexto;

    @BeforeEach
    void configurar() {
        paciente = Paciente.registrar("Laura Torres", "1020304050", "3001234567",
                "laura@example.com", LocalDate.of(1990, 1, 1));
        Medico medico = Medico.registrar("Dra. Ana Lopez", "Dermatologia", null, null);
        contexto = new ReservaValidationContext(paciente, medico, new FranjaHoraria(FECHA_CITA),
                AHORA, ZoneId.of("America/Bogota"));
    }

    @Test
    void debeBloquearAlPacienteDesdeLaTerceraPenalizacionEnLaVentanaMovil() {
        when(penalizaciones.contarDesde(paciente.id(), AHORA.minusSeconds(30L * 24 * 60 * 60)))
                .thenReturn(3L);

        assertThatThrownBy(() -> new PenalizacionesActivasValidator(penalizaciones).validar(contexto))
                .isInstanceOf(ConflictException.class)
                .extracting(error -> ((ConflictException) error).codigo())
                .isEqualTo("PACIENTE_BLOQUEADO");

        verify(penalizaciones).contarDesde(paciente.id(), AHORA.minusSeconds(30L * 24 * 60 * 60));
    }

    @Test
    void debePermitirReservaConSoloDosPenalizacionesRecientes() {
        when(penalizaciones.contarDesde(paciente.id(), AHORA.minusSeconds(30L * 24 * 60 * 60)))
                .thenReturn(2L);

        assertThatCode(() -> new PenalizacionesActivasValidator(penalizaciones).validar(contexto))
                .doesNotThrowAnyException();
    }

    @Test
    void debeDetectarConflictoDelPacienteAunqueSeaConOtroMedico() {
        when(citas.existeProgramadaParaPaciente(paciente.id(), FECHA_CITA)).thenReturn(true);

        assertThatThrownBy(() -> new ConflictoPacienteValidator(citas).validar(contexto))
                .isInstanceOf(ConflictException.class)
                .extracting(error -> ((ConflictException) error).codigo())
                .isEqualTo("FRANJA_PACIENTE_OCUPADA");
    }

    @Test
    void debePermitirPacienteSinConflictoDeFranja() {
        assertThatCode(() -> new ConflictoPacienteValidator(citas).validar(contexto))
                .doesNotThrowAnyException();
    }

    @Test
    void debePermitirMedicoSinConflictoDeFranja() {
        assertThatCode(() -> new DisponibilidadMedicoValidator(citas).validar(contexto))
                .doesNotThrowAnyException();
    }
}
