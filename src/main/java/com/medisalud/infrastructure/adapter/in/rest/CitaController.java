package com.medisalud.infrastructure.adapter.in.rest;

import com.medisalud.application.command.ReservarCitaCommand;
import com.medisalud.application.usecase.CancelarCitaUseCase;
import com.medisalud.application.usecase.ListarCitasUseCase;
import com.medisalud.application.usecase.ReprogramarCitaUseCase;
import com.medisalud.application.usecase.ReservarCitaUseCase;
import com.medisalud.domain.model.EstadoCita;
import com.medisalud.infrastructure.adapter.in.rest.request.ReprogramarCitaRequest;
import com.medisalud.infrastructure.adapter.in.rest.request.ReservarCitaRequest;
import com.medisalud.infrastructure.adapter.in.rest.response.CitaResponse;
import com.medisalud.infrastructure.adapter.in.rest.response.PaginaCitasResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

    private final ReservarCitaUseCase reservarCita;
    private final CancelarCitaUseCase cancelarCita;
    private final ReprogramarCitaUseCase reprogramarCita;
    private final ListarCitasUseCase listarCitas;
    private final ZoneId zonaHoraria;

    public CitaController(ReservarCitaUseCase reservarCita, CancelarCitaUseCase cancelarCita,
                          ReprogramarCitaUseCase reprogramarCita, ListarCitasUseCase listarCitas,
                          ZoneId zonaHoraria) {
        this.reservarCita = reservarCita;
        this.cancelarCita = cancelarCita;
        this.reprogramarCita = reprogramarCita;
        this.listarCitas = listarCitas;
        this.zonaHoraria = zonaHoraria;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponse reservar(@Valid @RequestBody ReservarCitaRequest request) {
        var cita = reservarCita.ejecutar(new ReservarCitaCommand(
                request.pacienteId(), request.medicoId(), request.fechaHora().toInstant()));
        return CitaResponse.desde(cita, zonaHoraria);
    }

    @PatchMapping("/{citaId}/cancelacion")
    public CitaResponse cancelar(@PathVariable UUID citaId) {
        return CitaResponse.desde(cancelarCita.ejecutar(citaId), zonaHoraria);
    }

    @PostMapping("/{citaId}/reprogramacion")
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponse reprogramar(@PathVariable UUID citaId,
                                    @Valid @RequestBody ReprogramarCitaRequest request) {
        return CitaResponse.desde(
                reprogramarCita.ejecutar(citaId, request.nuevaFechaHora().toInstant()), zonaHoraria);
    }

    @GetMapping
    public PaginaCitasResponse listar(
            @RequestParam(required = false) UUID medicoId,
            @RequestParam(required = false) UUID pacienteId,
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        return PaginaCitasResponse.desde(listarCitas.ejecutar(
                        medicoId,
                        pacienteId,
                        estado,
                        fechaInicio == null ? null : fechaInicio.toInstant(),
                        fechaFin == null ? null : fechaFin.toInstant(),
                        page,
                        size), zonaHoraria);
    }
}
