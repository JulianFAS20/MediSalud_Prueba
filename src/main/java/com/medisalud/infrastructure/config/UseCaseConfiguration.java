package com.medisalud.infrastructure.config;

import com.medisalud.application.port.TransaccionPort;
import com.medisalud.application.usecase.CancelarCitaUseCase;
import com.medisalud.application.usecase.ConsultarDisponibilidadUseCase;
import com.medisalud.application.usecase.ListarCitasUseCase;
import com.medisalud.application.usecase.RegistrarMedicoUseCase;
import com.medisalud.application.usecase.RegistrarPacienteUseCase;
import com.medisalud.application.usecase.ReprogramarCitaUseCase;
import com.medisalud.application.usecase.ReservarCitaUseCase;
import com.medisalud.application.validation.ConflictoPacienteValidator;
import com.medisalud.application.validation.DisponibilidadMedicoValidator;
import com.medisalud.application.validation.FechaNacimientoPacienteValidator;
import com.medisalud.application.validation.HorarioLaboralReservaValidator;
import com.medisalud.application.validation.PenalizacionesActivasValidator;
import com.medisalud.domain.port.CalendarioFestivosPort;
import com.medisalud.domain.port.CitaRepositoryPort;
import com.medisalud.domain.port.MedicoRepositoryPort;
import com.medisalud.domain.port.PacienteRepositoryPort;
import com.medisalud.domain.port.PenalizacionRepositoryPort;
import com.medisalud.domain.service.PenalizacionCancelacionStrategy;
import com.medisalud.domain.service.PenalizacionCancelacionTardiaStrategy;
import com.medisalud.domain.service.PoliticaHorarioAtencion;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Configuration
@EnableConfigurationProperties(MedisaludProperties.class)
public class UseCaseConfiguration {

    @Bean
    Clock reloj(MedisaludProperties properties) {
        return properties.getRelojFijo() == null
                ? Clock.systemUTC()
                : Clock.fixed(properties.getRelojFijo(), ZoneOffset.UTC);
    }

    @Bean
    ZoneId zonaHoraria(MedisaludProperties properties) {
        return ZoneId.of(properties.getZonaHoraria());
    }

    @Bean
    PenalizacionCancelacionStrategy estrategiaPenalizacion() {
        return new PenalizacionCancelacionTardiaStrategy();
    }

    @Bean
    PoliticaHorarioAtencion politicaHorarioAtencion(CalendarioFestivosPort calendarioFestivos) {
        return new PoliticaHorarioAtencion(calendarioFestivos);
    }

    @Bean
    RegistrarMedicoUseCase registrarMedico(MedicoRepositoryPort medicos, TransaccionPort transacciones) {
        return new RegistrarMedicoUseCase(medicos, transacciones);
    }

    @Bean
    RegistrarPacienteUseCase registrarPaciente(PacienteRepositoryPort pacientes, TransaccionPort transacciones) {
        return new RegistrarPacienteUseCase(pacientes, transacciones);
    }

    @Bean
    ReservarCitaUseCase reservarCita(
            PacienteRepositoryPort pacientes,
            MedicoRepositoryPort medicos,
            CitaRepositoryPort citas,
            PenalizacionRepositoryPort penalizaciones,
            PoliticaHorarioAtencion politicaHorario,
            TransaccionPort transacciones,
            Clock reloj,
            ZoneId zonaHoraria) {
        return new ReservarCitaUseCase(
                pacientes,
                medicos,
                citas,
                List.of(
                        new HorarioLaboralReservaValidator(politicaHorario),
                        new FechaNacimientoPacienteValidator(),
                        new PenalizacionesActivasValidator(penalizaciones),
                        new DisponibilidadMedicoValidator(citas),
                        new ConflictoPacienteValidator(citas)),
                transacciones,
                reloj,
                zonaHoraria);
    }

    @Bean
    CancelarCitaUseCase cancelarCita(
            CitaRepositoryPort citas,
            PenalizacionRepositoryPort penalizaciones,
            PenalizacionCancelacionStrategy estrategia,
            TransaccionPort transacciones,
            Clock reloj) {
        return new CancelarCitaUseCase(citas, penalizaciones, estrategia, transacciones, reloj);
    }

    @Bean
    ReprogramarCitaUseCase reprogramarCita(
            CitaRepositoryPort citas,
            CancelarCitaUseCase cancelar,
            ReservarCitaUseCase reservar,
            TransaccionPort transacciones,
            Clock reloj) {
        return new ReprogramarCitaUseCase(citas, cancelar, reservar, transacciones, reloj);
    }

    @Bean
    ConsultarDisponibilidadUseCase consultarDisponibilidad(
            MedicoRepositoryPort medicos,
            CitaRepositoryPort citas,
            PoliticaHorarioAtencion politicaHorario,
            Clock reloj,
            ZoneId zonaHoraria,
            MedisaludProperties properties) {
        return new ConsultarDisponibilidadUseCase(
                medicos, citas, politicaHorario, reloj, zonaHoraria,
                properties.getMaximoDiasDisponibilidad());
    }

    @Bean
    ListarCitasUseCase listarCitas(CitaRepositoryPort citas, MedisaludProperties properties) {
        return new ListarCitasUseCase(citas, properties.getMaximoTamanioPaginaCitas());
    }
}
