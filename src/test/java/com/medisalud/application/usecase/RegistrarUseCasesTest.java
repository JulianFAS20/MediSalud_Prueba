package com.medisalud.application.usecase;

import com.medisalud.application.command.RegistrarMedicoCommand;
import com.medisalud.application.command.RegistrarPacienteCommand;
import com.medisalud.application.port.TransaccionPort;
import com.medisalud.domain.exception.ConflictException;
import com.medisalud.domain.model.Medico;
import com.medisalud.domain.model.Paciente;
import com.medisalud.domain.port.MedicoRepositoryPort;
import com.medisalud.domain.port.PacienteRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarUseCasesTest {

    @Mock
    private PacienteRepositoryPort pacientes;
    @Mock
    private MedicoRepositoryPort medicos;

    @Test
    void debeRegistrarPacienteYConservarLaFechaDeNacimientoOpcional() {
        LocalDate nacimiento = LocalDate.of(1990, 1, 1);
        var command = new RegistrarPacienteCommand(
                "Laura Torres", "1020304050", "3001234567", "laura@example.com", nacimiento);
        when(pacientes.guardar(any(Paciente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        var resultado = new RegistrarPacienteUseCase(pacientes, new TransaccionSincrona()).ejecutar(command);

        assertThat(resultado.id()).isNotNull();
        assertThat(resultado.documentoIdentidad()).isEqualTo("1020304050");
        assertThat(resultado.fechaNacimiento()).isEqualTo(nacimiento);
        verify(pacientes).guardar(any(Paciente.class));
    }

    @Test
    void debeRechazarDocumentoDuplicadoSinIntentarGuardar() {
        var command = new RegistrarPacienteCommand(
                "Laura Torres", "1020304050", "3001234567", "laura@example.com", null);
        when(pacientes.existePorDocumento(command.documentoIdentidad())).thenReturn(true);

        assertThatThrownBy(() ->
                new RegistrarPacienteUseCase(pacientes, new TransaccionSincrona()).ejecutar(command))
                .isInstanceOf(ConflictException.class)
                .extracting(error -> ((ConflictException) error).codigo())
                .isEqualTo("DOCUMENTO_DUPLICADO");

        verify(pacientes, never()).guardar(any(Paciente.class));
    }

    @Test
    void debeRegistrarMedicoConContactoOpcional() {
        var command = new RegistrarMedicoCommand("Dra. Ana Lopez", "Dermatologia", null, null);
        when(medicos.guardar(any(Medico.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        var resultado = new RegistrarMedicoUseCase(medicos, new TransaccionSincrona()).ejecutar(command);

        assertThat(resultado.id()).isNotNull();
        assertThat(resultado.nombreCompleto()).isEqualTo("Dra. Ana Lopez");
        assertThat(resultado.telefono()).isNull();
        assertThat(resultado.email()).isNull();
    }

    private static final class TransaccionSincrona implements TransaccionPort {
        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            return operacion.get();
        }
    }
}
