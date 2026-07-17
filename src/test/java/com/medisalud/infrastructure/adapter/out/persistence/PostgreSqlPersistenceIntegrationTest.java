package com.medisalud.infrastructure.adapter.out.persistence;

import com.medisalud.MedisaludApplication;
import com.medisalud.application.command.ReservarCitaCommand;
import com.medisalud.application.port.TransaccionPort;
import com.medisalud.application.usecase.ReprogramarCitaUseCase;
import com.medisalud.application.usecase.ReservarCitaUseCase;
import com.medisalud.application.validation.DisponibilidadMedicoValidator;
import com.medisalud.application.validation.ReservaValidator;
import com.medisalud.domain.exception.ConflictException;
import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.EstadoCita;
import com.medisalud.domain.model.FiltroCitas;
import com.medisalud.domain.model.FranjaHoraria;
import com.medisalud.domain.model.Medico;
import com.medisalud.domain.model.Paciente;
import com.medisalud.domain.model.Paginacion;
import com.medisalud.domain.port.CalendarioFestivosPort;
import com.medisalud.domain.port.CitaRepositoryPort;
import com.medisalud.domain.port.MedicoRepositoryPort;
import com.medisalud.domain.port.PacienteRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("postgres")
@SpringBootTest(classes = MedisaludApplication.class)
class PostgreSqlPersistenceIntegrationTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("medisalud_test")
                    .withUsername("medisalud")
                    .withPassword("medisalud");

    @DynamicPropertySource
    static void configurarPostgreSql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRESQL::getDriverClassName);
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private CitaRepositoryPort citas;

    @Autowired
    private PacienteRepositoryPort pacientes;

    @Autowired
    private MedicoRepositoryPort medicos;

    @Autowired
    private CalendarioFestivosPort calendarioFestivos;

    @Autowired
    private TransaccionPort transacciones;

    @Autowired
    private ReprogramarCitaUseCase reprogramarCita;

    @Test
    void flywayDebeCrearElEsquemaCompletoEnPostgreSql() {
        Integer migracionesExitosas = jdbc.queryForObject("""
                select count(*) from flyway_schema_history
                where version = '1' and success = true
                """, Integer.class);
        Integer tablasDeNegocio = jdbc.queryForObject("""
                select count(*) from information_schema.tables
                where table_schema = 'public'
                  and table_name in ('medicos', 'pacientes', 'citas', 'penalizaciones')
                """, Integer.class);

        assertThat(migracionesExitosas).isEqualTo(1);
        assertThat(tablasDeNegocio).isEqualTo(4);
    }

    @Test
    void debePersistirTimestampWithTimeZoneSinAlterarElInstante() {
        String tipoFechaHora = jdbc.queryForObject("""
                select data_type from information_schema.columns
                where table_schema = 'public'
                  and table_name = 'citas'
                  and column_name = 'fecha_hora'
                """, String.class);
        Paciente paciente = guardarPaciente("TZ");
        Medico medico = guardarMedico("Zona Horaria");
        Instant instanteEsperado = OffsetDateTime.parse("2035-02-12T08:30:00-05:00").toInstant();
        Cita cita = citas.guardar(Cita.programar(
                paciente.id(), medico.id(), new FranjaHoraria(instanteEsperado)));

        OffsetDateTime fechaEnPostgreSql = jdbc.queryForObject(
                "select fecha_hora from citas where id = ?",
                (resultado, fila) -> resultado.getObject("fecha_hora", OffsetDateTime.class),
                cita.id());

        assertThat(tipoFechaHora).isEqualTo("timestamp with time zone");
        assertThat(fechaEnPostgreSql).isNotNull();
        assertThat(fechaEnPostgreSql.toInstant()).isEqualTo(instanteEsperado);
        assertThat(citas.buscarPorId(cita.id()).orElseThrow().franja().inicio())
                .isEqualTo(instanteEsperado);
    }

    @Test
    void debeAplicarRestriccionesUnicasDeMedicoYPacienteEnPostgreSql() {
        Instant primeraFranja = instante(siguienteDiaLaboral(2, 10), 8, 0);
        Medico medico = guardarMedico("Restriccion Medico PostgreSQL");
        Paciente primerPaciente = guardarPaciente("UK-M-1");
        Paciente segundoPaciente = guardarPaciente("UK-M-2");
        citas.guardar(Cita.programar(
                primerPaciente.id(), medico.id(), new FranjaHoraria(primeraFranja)));

        assertThatThrownBy(() -> citas.guardar(Cita.programar(
                segundoPaciente.id(), medico.id(), new FranjaHoraria(primeraFranja))))
                .isInstanceOf(DataIntegrityViolationException.class);

        Instant segundaFranja = instante(siguienteDiaLaboral(2, 11), 8, 0);
        Paciente paciente = guardarPaciente("UK-P-1");
        Medico primerMedico = guardarMedico("Restriccion Paciente PostgreSQL Uno");
        Medico segundoMedico = guardarMedico("Restriccion Paciente PostgreSQL Dos");
        citas.guardar(Cita.programar(
                paciente.id(), primerMedico.id(), new FranjaHoraria(segundaFranja)));

        assertThatThrownBy(() -> citas.guardar(Cita.programar(
                paciente.id(), segundoMedico.id(), new FranjaHoraria(segundaFranja))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void specificationsDebenCombinarFiltrosYPaginacionEnPostgreSql() {
        LocalDate fecha = siguienteDiaLaboral(3, 20);
        Instant primeraFranja = instante(fecha, 8, 0);
        Instant segundaFranja = instante(fecha, 8, 30);
        Instant terceraFranja = instante(fecha, 9, 0);
        Medico medico = guardarMedico("Specifications PostgreSQL");
        Paciente paciente = guardarPaciente("SPEC-1");
        Paciente otroPaciente = guardarPaciente("SPEC-2");

        citas.guardar(Cita.programar(
                paciente.id(), medico.id(), new FranjaHoraria(primeraFranja)));
        Cita cancelada = Cita.programar(
                paciente.id(), medico.id(), new FranjaHoraria(segundaFranja));
        cancelada.cancelar(Instant.now());
        cancelada = citas.guardar(cancelada);
        citas.guardar(Cita.programar(
                otroPaciente.id(), medico.id(), new FranjaHoraria(terceraFranja)));

        var resultado = citas.buscar(
                new FiltroCitas(
                        medico.id(), paciente.id(), EstadoCita.CANCELADA,
                        segundaFranja, segundaFranja),
                new Paginacion(0, 1));

        assertThat(resultado.totalElementos()).isEqualTo(1);
        assertThat(resultado.totalPaginas()).isEqualTo(1);
        assertThat(resultado.contenido()).singleElement()
                .extracting(Cita::id)
                .isEqualTo(cancelada.id());
    }

    @Test
    void reprogramacionFallidaDebeHacerRollbackEnPostgreSql() {
        LocalDate fecha = siguienteDiaLaboral(4, 30);
        Instant franjaOriginal = instante(fecha, 8, 0);
        Instant franjaOcupada = instante(fecha, 8, 30);
        Medico medico = guardarMedico("Rollback PostgreSQL");
        Paciente primerPaciente = guardarPaciente("ROLLBACK-1");
        Paciente segundoPaciente = guardarPaciente("ROLLBACK-2");
        Cita original = citas.guardar(Cita.programar(
                primerPaciente.id(), medico.id(), new FranjaHoraria(franjaOriginal)));
        citas.guardar(Cita.programar(
                segundoPaciente.id(), medico.id(), new FranjaHoraria(franjaOcupada)));

        assertThatThrownBy(() -> reprogramarCita.ejecutar(original.id(), franjaOcupada))
                .isInstanceOf(ConflictException.class)
                .extracting(error -> ((ConflictException) error).codigo())
                .isEqualTo("FRANJA_MEDICO_OCUPADA");

        Cita despuesDelRollback = citas.buscarPorId(original.id()).orElseThrow();
        assertThat(despuesDelRollback.estado()).isEqualTo(EstadoCita.PROGRAMADA);
        assertThat(despuesDelRollback.canceladaEn()).isNull();
    }

    @Test
    void dosReservasConcurrentesDebenConfirmarExactamenteUnaCita() throws Exception {
        LocalDate fecha = siguienteDiaLaboral(5, 40);
        Instant franja = instante(fecha, 9, 0);
        Medico medico = guardarMedico("Concurrencia PostgreSQL");
        Paciente primerPaciente = guardarPaciente("RACE-1");
        Paciente segundoPaciente = guardarPaciente("RACE-2");
        CyclicBarrier barrera = new CyclicBarrier(2);
        ReservaValidator sincronizador = contexto -> esperarEnBarrera(barrera);
        ReservarCitaUseCase reservarConcurrentemente = new ReservarCitaUseCase(
                pacientes,
                medicos,
                citas,
                List.of(new DisponibilidadMedicoValidator(citas), sincronizador),
                transacciones,
                Clock.systemUTC(),
                ZONA);
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);

        try {
            Future<ResultadoReserva> primera = ejecutor.submit(() -> intentarReservar(
                    reservarConcurrentemente, primerPaciente.id(), medico.id(), franja));
            Future<ResultadoReserva> segunda = ejecutor.submit(() -> intentarReservar(
                    reservarConcurrentemente, segundoPaciente.id(), medico.id(), franja));
            List<ResultadoReserva> resultados = List.of(
                    primera.get(20, TimeUnit.SECONDS),
                    segunda.get(20, TimeUnit.SECONDS));

            assertThat(resultados).filteredOn(ResultadoReserva::exitosa).hasSize(1);
            Throwable error = resultados.stream()
                    .filter(resultado -> !resultado.exitosa())
                    .map(ResultadoReserva::error)
                    .findFirst()
                    .orElseThrow();
            assertThat(error).isInstanceOf(DataIntegrityViolationException.class);
            assertThat(estadoSqlRaiz(error)).isEqualTo("23505");

            var persistidas = citas.buscar(
                    new FiltroCitas(medico.id(), null, EstadoCita.PROGRAMADA, franja, franja),
                    new Paginacion(0, 10));
            assertThat(persistidas.totalElementos()).isEqualTo(1);
        } finally {
            ejecutor.shutdownNow();
        }
    }

    private ResultadoReserva intentarReservar(ReservarCitaUseCase useCase, UUID pacienteId,
                                               UUID medicoId, Instant franja) {
        try {
            useCase.ejecutar(new ReservarCitaCommand(pacienteId, medicoId, franja));
            return new ResultadoReserva(true, null);
        } catch (Throwable error) {
            return new ResultadoReserva(false, error);
        }
    }

    private static void esperarEnBarrera(CyclicBarrier barrera) {
        try {
            barrera.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La prueba concurrente fue interrumpida", error);
        } catch (BrokenBarrierException | TimeoutException error) {
            throw new IllegalStateException("No fue posible sincronizar las reservas", error);
        }
    }

    private static String estadoSqlRaiz(Throwable error) {
        Throwable causa = error;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        return causa instanceof SQLException sqlException ? sqlException.getSQLState() : null;
    }

    private Paciente guardarPaciente(String prefijoDocumento) {
        String sufijo = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String documento = prefijoDocumento + "-" + sufijo;
        return pacientes.guardar(Paciente.registrar(
                "Paciente PostgreSQL", documento, "3001234567",
                documento.toLowerCase() + "@example.com", null));
    }

    private Medico guardarMedico(String contexto) {
        return medicos.guardar(Medico.registrar(
                "Doctor " + contexto, "Medicina General", null, null));
    }

    private LocalDate siguienteDiaLaboral(int aniosEnElFuturo, int diasAdicionales) {
        LocalDate fecha = LocalDate.now(ZONA).plusYears(aniosEnElFuturo).plusDays(diasAdicionales);
        while (fecha.getDayOfWeek() == DayOfWeek.SUNDAY
                || calendarioFestivos.esFestivo(fecha)) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }

    private static Instant instante(LocalDate fecha, int hora, int minuto) {
        return fecha.atTime(hora, minuto).atZone(ZONA).toInstant();
    }

    private record ResultadoReserva(boolean exitosa, Throwable error) {
    }
}
