package com.medisalud.application.usecase;

import com.medisalud.application.command.RegistrarPacienteCommand;
import com.medisalud.application.dto.PacienteDto;
import com.medisalud.application.port.TransaccionPort;
import com.medisalud.domain.exception.ConflictException;
import com.medisalud.domain.model.Paciente;
import com.medisalud.domain.port.PacienteRepositoryPort;

public final class RegistrarPacienteUseCase {

    private final PacienteRepositoryPort pacientes;
    private final TransaccionPort transacciones;

    public RegistrarPacienteUseCase(PacienteRepositoryPort pacientes, TransaccionPort transacciones) {
        this.pacientes = pacientes;
        this.transacciones = transacciones;
    }

    public PacienteDto ejecutar(RegistrarPacienteCommand comando) {
        return transacciones.ejecutar(() -> {
            if (pacientes.existePorDocumento(comando.documentoIdentidad())) {
                throw new ConflictException("DOCUMENTO_DUPLICADO",
                        "Ya existe un paciente con ese documento de identidad");
            }
            Paciente paciente = Paciente.registrar(comando.nombreCompleto(), comando.documentoIdentidad(),
                    comando.telefono(), comando.email(), comando.fechaNacimiento());
            return PacienteDto.desde(pacientes.guardar(paciente));
        });
    }
}
