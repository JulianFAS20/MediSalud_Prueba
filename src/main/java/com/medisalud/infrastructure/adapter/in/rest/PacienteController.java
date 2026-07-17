package com.medisalud.infrastructure.adapter.in.rest;

import com.medisalud.application.command.RegistrarPacienteCommand;
import com.medisalud.application.dto.PacienteDto;
import com.medisalud.application.usecase.RegistrarPacienteUseCase;
import com.medisalud.infrastructure.adapter.in.rest.request.RegistrarPacienteRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pacientes")
public class PacienteController {

    private final RegistrarPacienteUseCase registrarPaciente;

    public PacienteController(RegistrarPacienteUseCase registrarPaciente) {
        this.registrarPaciente = registrarPaciente;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteDto registrar(@Valid @RequestBody RegistrarPacienteRequest request) {
        return registrarPaciente.ejecutar(new RegistrarPacienteCommand(
                request.nombreCompleto(), request.documentoIdentidad(), request.telefono(),
                request.email(), request.fechaNacimiento()));
    }
}
