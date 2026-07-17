package com.medisalud.application.validation;

import com.medisalud.domain.exception.ValidationException;
import com.medisalud.domain.model.FranjaHoraria;
import com.medisalud.domain.model.Medico;
import com.medisalud.domain.model.Paciente;
import com.medisalud.domain.port.CalendarioFestivosPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HorarioLaboralReservaValidatorTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Instant AHORA = local(2026, 6, 8, 7, 0);
    private static final Paciente PACIENTE = Paciente.registrar(
            "Laura Torres", "1020304050", "3001234567", "laura@example.com", null);
    private static final Medico MEDICO = Medico.registrar(
            "Dra. Ana Lopez", "Dermatologia", null, null);

    @ParameterizedTest(name = "acepta {0}")
    @MethodSource("franjasValidas")
    void debeAceptarLosLimitesIncluidosDelHorario(String descripcion, Instant inicio) {
        HorarioLaboralReservaValidator validator = new HorarioLaboralReservaValidator(fecha -> false);

        assertThatCode(() -> validator.validar(contexto(inicio, AHORA)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "rechaza {0} con {2}")
    @MethodSource("franjasInvalidas")
    void debeRechazarFranjasInvalidas(String descripcion, Instant inicio, String codigo) {
        HorarioLaboralReservaValidator validator = new HorarioLaboralReservaValidator(fecha -> false);

        assertThatThrownBy(() -> validator.validar(contexto(inicio, AHORA)))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo(codigo);
    }

    @Test
    void debeRechazarUnFestivoAunqueSeaDiaDeSemana() {
        LocalDate festivo = LocalDate.of(2026, 6, 10);
        CalendarioFestivosPort calendario = festivo::equals;
        HorarioLaboralReservaValidator validator = new HorarioLaboralReservaValidator(calendario);

        assertThatThrownBy(() -> validator.validar(contexto(local(2026, 6, 10, 8, 0), AHORA)))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo("DIA_NO_LABORAL");
    }

    @Test
    void debeRechazarUnaCitaEnElInstanteActual() {
        HorarioLaboralReservaValidator validator = new HorarioLaboralReservaValidator(fecha -> false);
        Instant ahoraLaboral = local(2026, 6, 10, 8, 0);

        assertThatThrownBy(() -> validator.validar(contexto(ahoraLaboral, ahoraLaboral)))
                .isInstanceOf(ValidationException.class)
                .extracting(error -> ((ValidationException) error).codigo())
                .isEqualTo("CITA_NO_FUTURA");
    }

    private static Stream<Arguments> franjasValidas() {
        return Stream.of(
                Arguments.of("apertura entre semana", local(2026, 6, 10, 8, 0)),
                Arguments.of("ultima franja entre semana", local(2026, 6, 10, 17, 30)),
                Arguments.of("apertura del sabado", local(2026, 6, 13, 8, 0)),
                Arguments.of("ultima franja del sabado", local(2026, 6, 13, 12, 30)));
    }

    private static Stream<Arguments> franjasInvalidas() {
        return Stream.of(
                Arguments.of("minuto distinto de 00 o 30", local(2026, 6, 10, 8, 15), "FRANJA_INVALIDA"),
                Arguments.of("segundos distintos de cero", local(2026, 6, 10, 8, 0).plusSeconds(1), "FRANJA_INVALIDA"),
                Arguments.of("domingo", local(2026, 6, 14, 8, 0), "DIA_NO_LABORAL"),
                Arguments.of("antes de apertura", local(2026, 6, 10, 7, 30), "FUERA_DE_HORARIO"),
                Arguments.of("despues del cierre semanal", local(2026, 6, 10, 18, 0), "FUERA_DE_HORARIO"),
                Arguments.of("despues del cierre sabado", local(2026, 6, 13, 13, 0), "FUERA_DE_HORARIO"));
    }

    private static ReservaValidationContext contexto(Instant inicio, Instant ahora) {
        return new ReservaValidationContext(PACIENTE, MEDICO, new FranjaHoraria(inicio), ahora, ZONA);
    }

    private static Instant local(int anio, int mes, int dia, int hora, int minuto) {
        return LocalDateTime.of(anio, mes, dia, hora, minuto).atZone(ZONA).toInstant();
    }
}
