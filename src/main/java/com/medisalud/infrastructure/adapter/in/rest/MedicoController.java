package com.medisalud.infrastructure.adapter.in.rest;

import com.medisalud.application.command.RegistrarMedicoCommand;
import com.medisalud.application.usecase.ConsultarDisponibilidadUseCase;
import com.medisalud.application.usecase.RegistrarMedicoUseCase;
import com.medisalud.infrastructure.adapter.in.rest.documentation.OpenApiExamples;
import com.medisalud.infrastructure.adapter.in.rest.error.ApiError;
import com.medisalud.infrastructure.adapter.in.rest.request.RegistrarMedicoRequest;
import com.medisalud.infrastructure.adapter.in.rest.response.DisponibilidadResponse;
import com.medisalud.infrastructure.adapter.in.rest.response.MedicoResponse;
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
@RequestMapping(value = "/api/v1/medicos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Medicos", description = "Registro de medicos y consulta de disponibilidad")
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "registrarMedico", summary = "Registrar medico",
            description = "Crea un medico con especialidad y datos de contacto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Medico registrado",
                    content = @Content(schema = @Schema(implementation = MedicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.REQUEST_INVALIDO))),
            @ApiResponse(responseCode = "415", description = "Content-Type no soportado",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.TIPO_CONTENIDO_NO_SOPORTADO))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERROR_INTERNO)))
    })
    public MedicoResponse registrar(@Valid @RequestBody RegistrarMedicoRequest request) {
        return MedicoResponse.desde(registrarMedico.ejecutar(new RegistrarMedicoCommand(
                request.nombreCompleto(), request.especialidad(), request.telefono(), request.email())));
    }

    @GetMapping("/{medicoId}/disponibilidad")
    @Operation(operationId = "consultarDisponibilidadMedico", summary = "Consultar disponibilidad",
            description = "Devuelve las franjas de 30 minutos disponibles dentro de un rango de hasta 90 dias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad calculada",
                    content = @Content(schema = @Schema(implementation = DisponibilidadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Rango o parametros invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.REQUEST_INVALIDO))),
            @ApiResponse(responseCode = "404", description = "Medico inexistente",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.NO_ENCONTRADO))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERROR_INTERNO)))
    })
    public DisponibilidadResponse consultarDisponibilidad(
            @Parameter(description = "Identificador del medico",
                    example = "00000000-0000-0000-0000-000000000001") @PathVariable UUID medicoId,
            @Parameter(description = "Primera fecha del rango", example = "2027-02-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Ultima fecha del rango", example = "2027-02-06")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return DisponibilidadResponse.desde(
                consultarDisponibilidad.ejecutar(medicoId, fechaInicio, fechaFin), zonaHoraria);
    }
}
