package com.medisalud.application.usecase;

import com.medisalud.application.command.ReservarCitaCommand;
import com.medisalud.application.port.TransaccionPort;
import com.medisalud.application.validation.ConflictoPacienteValidator;
import com.medisalud.application.validation.DisponibilidadMedicoValidator;
import com.medisalud.application.validation.FechaNacimientoPacienteValidator;
import com.medisalud.application.validation.HorarioLaboralReservaValidator;
import com.medisalud.application.validation.PenalizacionesActivasValidator;
import com.medisalud.domain.exception.ConflictException;
import com.medisalud.domain.exception.NotFoundException;
import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.EstadoCita;
import com.medisalud.domain.model.Medico;
import com.medisalud.domain.model.Paciente;
import com.medisalud.domain.port.CalendarioFestivosPort;
import com.medisalud.domain.port.CitaRepositoryPort;
import com.medisalud.domain.port.MedicoRepositoryPort;
import com.medisalud.domain.port.PacienteRepositoryPort;
import com.medisalud.domain.port.PenalizacionRepositoryPort;
import com.medisalud.domain.service.PoliticaHorarioAtencion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservarCitaUseCaseTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Instant AHORA = Instant.parse("2026-06-10T12:00:00Z");
    private static final Instant FECHA_CITA = Instant.parse("2026-06-10T14:00:00Z");

    @Mock
    private PacienteRepositoryPort pacientes;
    @Mock
    private MedicoRepositoryPort medicos;
    @Mock
    private CitaRepositoryPort citas;
    @Mock
    private PenalizacionRepositoryPort penalizaciones;
    @Mock
    private CalendarioFestivosPort festivos;

    private Paciente paciente;
    private Medico medico;
    private ReservarCitaUseCase useCase;

    @BeforeEach
    void configurar() {
        paciente = Paciente.restaurar(UUID.randomUUID(), "Laura Torres", "1020304050", "3001234567",
                "laura@example.com", LocalDate.of(1990, 1, 1));
        medico = Medico.restaurar(UUID.randomUUID(), "Dra. Maria Gonzalez", "Cardiologia",
                "5551001", "maria@medisalud.com");

        useCase = new ReservarCitaUseCase(
                pacientes,
                medicos,
                citas,
                List.of(
                        new HorarioLaboralReservaValidator(new PoliticaHorarioAtencion(festivos)),
                        new FechaNacimientoPacienteValidator(),
                        new PenalizacionesActivasValidator(penalizaciones),
                        new DisponibilidadMedicoValidator(citas),
                        new ConflictoPacienteValidator(citas)),
                new TransaccionSincrona(),
                Clock.fixed(AHORA, ZoneOffset.UTC),
                ZONA);

        lenient().when(pacientes.buscarPorId(paciente.id())).thenReturn(Optional.of(paciente));
        lenient().when(medicos.buscarPorId(medico.id())).thenReturn(Optional.of(medico));
    }

    @Test
    void debeReservarCuandoTodasLasReglasSeCumplen() {
        when(citas.guardar(any(Cita.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        var resultado = useCase.ejecutar(new ReservarCitaCommand(paciente.id(), medico.id(), FECHA_CITA));

        assertThat(resultado.estado()).isEqualTo(EstadoCita.PROGRAMADA);
        assertThat(resultado.pacienteId()).isEqualTo(paciente.id());
        assertThat(resultado.medicoId()).isEqualTo(medico.id());
        assertThat(resultado.fechaHora()).isEqualTo(FECHA_CITA);
        verify(citas).guardar(any(Cita.class));
    }

    @Test
    void debeRechazarCuandoElMedicoYaTieneLaFranjaOcupada() {
        when(citas.existeProgramadaParaMedico(medico.id(), FECHA_CITA)).thenReturn(true);

        assertThatThrownBy(() ->
                useCase.ejecutar(new ReservarCitaCommand(paciente.id(), medico.id(), FECHA_CITA)))
                .isInstanceOf(ConflictException.class)
                .hasMessage("El medico ya tiene una cita programada en esa franja");

        verify(citas, never()).guardar(any(Cita.class));
    }

    @Test
    void debeRechazarPacienteConTresPenalizacionesRecientes() {
        when(penalizaciones.contarDesde(any(UUID.class), any(Instant.class))).thenReturn(3L);

        assertThatThrownBy(() ->
                useCase.ejecutar(new ReservarCitaCommand(paciente.id(), medico.id(), FECHA_CITA)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("3 o mas penalizaciones");

        verify(citas, never()).guardar(any(Cita.class));
    }

    @Test
    void debeRechazarCuandoElPacienteNoExisteAntesDeConsultarElMedico() {
        when(pacientes.buscarPorId(paciente.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.ejecutar(new ReservarCitaCommand(paciente.id(), medico.id(), FECHA_CITA)))
                .isInstanceOf(NotFoundException.class)
                .extracting(error -> ((NotFoundException) error).codigo())
                .isEqualTo("PACIENTE_NO_ENCONTRADO");

        verify(medicos, never()).buscarPorId(any(UUID.class));
        verify(citas, never()).guardar(any(Cita.class));
    }

    @Test
    void debeRechazarCuandoElMedicoNoExiste() {
        when(medicos.buscarPorId(medico.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.ejecutar(new ReservarCitaCommand(paciente.id(), medico.id(), FECHA_CITA)))
                .isInstanceOf(NotFoundException.class)
                .extracting(error -> ((NotFoundException) error).codigo())
                .isEqualTo("MEDICO_NO_ENCONTRADO");

        verify(citas, never()).guardar(any(Cita.class));
    }

    @Test
    void debeRechazarConflictoDelPacienteEnLaMismaFranja() {
        when(citas.existeProgramadaParaPaciente(paciente.id(), FECHA_CITA)).thenReturn(true);

        assertThatThrownBy(() ->
                useCase.ejecutar(new ReservarCitaCommand(paciente.id(), medico.id(), FECHA_CITA)))
                .isInstanceOf(ConflictException.class)
                .extracting(error -> ((ConflictException) error).codigo())
                .isEqualTo("FRANJA_PACIENTE_OCUPADA");

        verify(citas, never()).guardar(any(Cita.class));
    }

    private static final class TransaccionSincrona implements TransaccionPort {
        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            return operacion.get();
        }
    }
}
