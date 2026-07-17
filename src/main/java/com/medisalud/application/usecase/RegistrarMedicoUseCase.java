package com.medisalud.application.usecase;

import com.medisalud.application.command.RegistrarMedicoCommand;
import com.medisalud.application.dto.MedicoDto;
import com.medisalud.application.port.TransaccionPort;
import com.medisalud.domain.model.Medico;
import com.medisalud.domain.port.MedicoRepositoryPort;

public final class RegistrarMedicoUseCase {

    private final MedicoRepositoryPort medicos;
    private final TransaccionPort transacciones;

    public RegistrarMedicoUseCase(MedicoRepositoryPort medicos, TransaccionPort transacciones) {
        this.medicos = medicos;
        this.transacciones = transacciones;
    }

    public MedicoDto ejecutar(RegistrarMedicoCommand comando) {
        return transacciones.ejecutar(() -> {
            Medico medico = Medico.registrar(comando.nombreCompleto(), comando.especialidad(),
                    comando.telefono(), comando.email());
            return MedicoDto.desde(medicos.guardar(medico));
        });
    }
}
