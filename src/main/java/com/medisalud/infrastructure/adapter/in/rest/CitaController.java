package com.medisalud.infrastructure.adapter.in.rest;

import com.medisalud.application.command.ReservarCitaCommand;
import com.medisalud.application.usecase.CancelarCitaUseCase;
import com.medisalud.application.usecase.ListarCitasUseCase;
import com.medisalud.application.usecase.ReprogramarCitaUseCase;
import com.medisalud.application.usecase.ReservarCitaUseCase;
import com.medisalud.domain.model.EstadoCita;
import com.medisalud.infrastructure.adapter.in.rest.documentation.OpenApiExamples;
import com.medisalud.infrastructure.adapter.in.rest.error.ApiError;
import com.medisalud.infrastructure.adapter.in.rest.request.ReprogramarCitaRequest;
import com.medisalud.infrastructure.adapter.in.rest.request.ReservarCitaRequest;
import com.medisalud.infrastructure.adapter.in.rest.response.CitaResponse;
import com.medisalud.infrastructure.adapter.in.rest.response.PaginaCitasResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping(value = "/api/v1/citas", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Citas", description = "Reserva, cancelacion, reprogramacion y consulta de citas")
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "reservarCita", summary = "Reservar cita",
            description = "Reserva una franja disponible para un paciente y medico")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cita programada",
                    content = @Content(schema = @Schema(implementation = CitaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Horario o datos invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.REQUEST_INVALIDO))),
            @ApiResponse(responseCode = "404", description = "Paciente o medico inexistente",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.NO_ENCONTRADO))),
            @ApiResponse(responseCode = "409", description = "Franja ocupada o paciente penalizado",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.CONFLICTO))),
            @ApiResponse(responseCode = "415", description = "Content-Type no soportado",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.TIPO_CONTENIDO_NO_SOPORTADO))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERROR_INTERNO)))
    })
    public CitaResponse reservar(@Valid @RequestBody ReservarCitaRequest request) {
        var cita = reservarCita.ejecutar(new ReservarCitaCommand(
                request.pacienteId(), request.medicoId(), request.fechaHora().toInstant()));
        return CitaResponse.desde(cita, zonaHoraria);
    }

    @PatchMapping("/{citaId}/cancelacion")
    @Operation(operationId = "cancelarCita", summary = "Cancelar cita",
            description = "Cancela una cita programada y aplica RN-05 cuando corresponde")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita cancelada",
                    content = @Content(schema = @Schema(implementation = CitaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Identificador invalido",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.REQUEST_INVALIDO))),
            @ApiResponse(responseCode = "404", description = "Cita inexistente",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.NO_ENCONTRADO))),
            @ApiResponse(responseCode = "409", description = "Cita no cancelable o conflicto concurrente",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.CONFLICTO))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERROR_INTERNO)))
    })
    public CitaResponse cancelar(
            @Parameter(description = "Identificador de la cita",
                    example = "f37d3d33-29a0-40aa-971a-721632c18c47") @PathVariable UUID citaId) {
        return CitaResponse.desde(cancelarCita.ejecutar(citaId), zonaHoraria);
    }

    @PostMapping(value = "/{citaId}/reprogramacion", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "reprogramarCita", summary = "Reprogramar cita",
            description = "Cancela la cita anterior y crea una nueva dentro de una unica transaccion")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Nueva cita programada",
                    content = @Content(schema = @Schema(implementation = CitaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nuevo horario invalido",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.REQUEST_INVALIDO))),
            @ApiResponse(responseCode = "404", description = "Cita inexistente",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.NO_ENCONTRADO))),
            @ApiResponse(responseCode = "409", description = "Nueva franja ocupada o conflicto concurrente",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.CONFLICTO))),
            @ApiResponse(responseCode = "415", description = "Content-Type no soportado",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.TIPO_CONTENIDO_NO_SOPORTADO))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERROR_INTERNO)))
    })
    public CitaResponse reprogramar(
                                    @Parameter(description = "Identificador de la cita",
                                            example = "f37d3d33-29a0-40aa-971a-721632c18c47")
                                    @PathVariable UUID citaId,
                                    @Valid @RequestBody ReprogramarCitaRequest request) {
        return CitaResponse.desde(
                reprogramarCita.ejecutar(citaId, request.nuevaFechaHora().toInstant()), zonaHoraria);
    }

    @GetMapping
    @Operation(operationId = "listarCitas", summary = "Listar citas",
            description = "Consulta citas mediante filtros opcionales y paginacion")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagina de citas",
                    content = @Content(schema = @Schema(implementation = PaginaCitasResponse.class))),
            @ApiResponse(responseCode = "400", description = "Filtros o paginacion invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.REQUEST_INVALIDO))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERROR_INTERNO)))
    })
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
