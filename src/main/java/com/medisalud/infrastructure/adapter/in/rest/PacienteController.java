package com.medisalud.infrastructure.adapter.in.rest;

import com.medisalud.application.command.RegistrarPacienteCommand;
import com.medisalud.application.usecase.RegistrarPacienteUseCase;
import com.medisalud.infrastructure.adapter.in.rest.documentation.OpenApiExamples;
import com.medisalud.infrastructure.adapter.in.rest.error.ApiError;
import com.medisalud.infrastructure.adapter.in.rest.request.RegistrarPacienteRequest;
import com.medisalud.infrastructure.adapter.in.rest.response.PacienteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/pacientes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pacientes", description = "Registro de pacientes")
public class PacienteController {

    private final RegistrarPacienteUseCase registrarPaciente;

    public PacienteController(RegistrarPacienteUseCase registrarPaciente) {
        this.registrarPaciente = registrarPaciente;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "registrarPaciente", summary = "Registrar paciente",
            description = "Crea un paciente con documento unico")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paciente registrado",
                    content = @Content(schema = @Schema(implementation = PacienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.REQUEST_INVALIDO))),
            @ApiResponse(responseCode = "409", description = "Documento ya registrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.DOCUMENTO_DUPLICADO))),
            @ApiResponse(responseCode = "415", description = "Content-Type no soportado",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.TIPO_CONTENIDO_NO_SOPORTADO))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERROR_INTERNO)))
    })
    public PacienteResponse registrar(@Valid @RequestBody RegistrarPacienteRequest request) {
        return PacienteResponse.desde(registrarPaciente.ejecutar(new RegistrarPacienteCommand(
                request.nombreCompleto(), request.documentoIdentidad(), request.telefono(),
                request.email(), request.fechaNacimiento())));
    }
}
