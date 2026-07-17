package com.medisalud.infrastructure.adapter.in.rest;

import com.medisalud.application.command.RegistrarMedicoCommand;
import com.medisalud.application.dto.MedicoDto;
import com.medisalud.application.usecase.ConsultarDisponibilidadUseCase;
import com.medisalud.application.usecase.RegistrarMedicoUseCase;
import com.medisalud.infrastructure.adapter.in.rest.request.RegistrarMedicoRequest;
import com.medisalud.infrastructure.adapter.in.rest.response.DisponibilidadResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/medicos")
public class MedicoController {

    private final RegistrarMedicoUseCase registrarMedico;
    private final ConsultarDisponibilidadUseCase consultarDisponibilidad;
    private final ZoneId zonaHoraria;

    public MedicoController(RegistrarMedicoUseCase registrarMedico,
                            ConsultarDisponibilidadUseCase consultarDisponibilidad,
                            ZoneId zonaHoraria) {
        this.registrarMedico = registrarMedico;
        this.consultarDisponibilidad = consultarDisponibilidad;
        this.zonaHoraria = zonaHoraria;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicoDto registrar(@Valid @RequestBody RegistrarMedicoRequest request) {
        return registrarMedico.ejecutar(new RegistrarMedicoCommand(
                request.nombreCompleto(), request.especialidad(), request.telefono(), request.email()));
    }

    @GetMapping("/{medicoId}/disponibilidad")
    public DisponibilidadResponse consultarDisponibilidad(
            @PathVariable UUID medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return DisponibilidadResponse.desde(
                consultarDisponibilidad.ejecutar(medicoId, fechaInicio, fechaFin), zonaHoraria);
    }
}
