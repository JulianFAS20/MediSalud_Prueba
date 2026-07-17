package com.medisalud.application.usecase;

import com.medisalud.application.command.ReservarCitaCommand;
import com.medisalud.application.dto.CitaDto;
import com.medisalud.application.port.TransaccionPort;
import com.medisalud.application.validation.ReservaValidationContext;
import com.medisalud.application.validation.ReservaValidator;
import com.medisalud.domain.exception.NotFoundException;
import com.medisalud.domain.model.Cita;
import com.medisalud.domain.model.FranjaHoraria;
import com.medisalud.domain.model.Medico;
import com.medisalud.domain.model.Paciente;
import com.medisalud.domain.port.CitaRepositoryPort;
import com.medisalud.domain.port.MedicoRepositoryPort;
import com.medisalud.domain.port.PacienteRepositoryPort;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public final class ReservarCitaUseCase {

    private final PacienteRepositoryPort pacientes;
    private final MedicoRepositoryPort medicos;
    private final CitaRepositoryPort citas;
    private final List<ReservaValidator> validadores;
    private final TransaccionPort transacciones;
    private final Clock reloj;
    private final ZoneId zonaHoraria;

    public ReservarCitaUseCase(PacienteRepositoryPort pacientes, MedicoRepositoryPort medicos,
                               CitaRepositoryPort citas, List<ReservaValidator> validadores,
                               TransaccionPort transacciones, Clock reloj, ZoneId zonaHoraria) {
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.citas = citas;
        this.validadores = List.copyOf(validadores);
        this.transacciones = transacciones;
        this.reloj = reloj;
        this.zonaHoraria = zonaHoraria;
    }

    public CitaDto ejecutar(ReservarCitaCommand comando) {
        return transacciones.ejecutar(() -> reservar(comando, reloj.instant()));
    }

    CitaDto reservar(ReservarCitaCommand comando, Instant ahora) {
        Paciente paciente = pacientes.buscarPorId(comando.pacienteId())
                .orElseThrow(() -> new NotFoundException("PACIENTE_NO_ENCONTRADO", "El paciente no existe"));
        Medico medico = medicos.buscarPorId(comando.medicoId())
                .orElseThrow(() -> new NotFoundException("MEDICO_NO_ENCONTRADO", "El medico no existe"));
        FranjaHoraria franja = new FranjaHoraria(comando.fechaHora());
        ReservaValidationContext contexto = new ReservaValidationContext(paciente, medico, franja, ahora, zonaHoraria);
        validadores.forEach(validador -> validador.validar(contexto));
        return CitaDto.desde(citas.guardar(Cita.programar(paciente.id(), medico.id(), franja)));
    }
}
