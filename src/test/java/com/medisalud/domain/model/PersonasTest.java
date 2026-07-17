package com.medisalud.domain.model;

import com.medisalud.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonasTest {

    @Test
    void debeNormalizarLosDatosDelMedicoYAdmitirContactoOpcional() {
        Medico medico = Medico.registrar("  Dra. Ana Lopez  ", "  Dermatologia  ", "  ", null);

        assertThat(medico.nombreCompleto()).isEqualTo("Dra. Ana Lopez");
        assertThat(medico.especialidad()).isEqualTo("Dermatologia");
        assertThat(medico.telefono()).isNull();
        assertThat(medico.email()).isNull();
    }

    @Test
    void debeNormalizarEmailYConservarTelefonoDelPaciente() {
        Paciente paciente = Paciente.registrar("  Laura Torres  ", "  1020304050  ",
                "  +57 300-123-4567  ", "  LAURA@EXAMPLE.COM  ", null);

        assertThat(paciente.nombreCompleto()).isEqualTo("Laura Torres");
        assertThat(paciente.documentoIdentidad()).isEqualTo("1020304050");
        assertThat(paciente.telefono()).isEqualTo("+57 300-123-4567");
        assertThat(paciente.email()).isEqualTo("laura@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"correo", "usuario@", "@example.com", "usuario@example"})
    void debeRechazarEmailsInvalidos(String email) {
        assertThatThrownBy(() -> Medico.registrar("Doctor Valido", "Cardiologia", null, email))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo("EMAIL_INVALIDO");
    }

    @Test
    void debeExigirEmailYTelefonoAlPaciente() {
        assertThatThrownBy(() -> Paciente.registrar(
                "Paciente Valido", "1234567", null, "paciente@example.com", null))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo("TELEFONO_OBLIGATORIO");

        assertThatThrownBy(() -> Paciente.registrar(
                "Paciente Valido", "1234567", "3001234567", " ", null))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo("EMAIL_OBLIGATORIO");
    }

    @Test
    void debeRechazarCamposConLongitudInvalidaYTelefonosSinSuficientesDigitos() {
        assertThatThrownBy(() -> Medico.registrar("AB", "Cardiologia", null, null))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo("LONGITUD_INVALIDA");

        assertThatThrownBy(() -> Paciente.registrar(
                "Paciente Valido", "1234567", "555-12", "paciente@example.com", null))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo("TELEFONO_INVALIDO");
    }

    @Test
    void debeValidarFechaDeNacimientoContraLaFechaLocalActual() {
        Paciente sinFecha = pacienteConFecha(null);
        Paciente nacidoHoy = pacienteConFecha(LocalDate.of(2026, 6, 10));
        Paciente fechaFutura = pacienteConFecha(LocalDate.of(2026, 6, 11));

        assertThatCode(() -> sinFecha.validarFechaNacimiento(LocalDate.of(2026, 6, 10)))
                .doesNotThrowAnyException();
        assertThatCode(() -> nacidoHoy.validarFechaNacimiento(LocalDate.of(2026, 6, 10)))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> fechaFutura.validarFechaNacimiento(LocalDate.of(2026, 6, 10)))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo("FECHA_NACIMIENTO_FUTURA");
    }

    private Paciente pacienteConFecha(LocalDate fechaNacimiento) {
        return Paciente.registrar("Laura Torres", "1020304050", "3001234567",
                "laura@example.com", fechaNacimiento);
    }
}
