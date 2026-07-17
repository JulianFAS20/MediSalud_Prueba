package com.medisalud.infrastructure.adapter.in.rest.documentation;

public final class OpenApiExamples {

    public static final String REQUEST_INVALIDO = """
            {
              "timestamp": "2026-06-10T15:30:00Z",
              "status": 400,
              "error": "Bad Request",
              "codigo": "REQUEST_INVALIDO",
              "mensaje": "La solicitud contiene campos invalidos",
              "path": "/api/v1/pacientes",
              "erroresCampo": [
                {"campo": "email", "mensaje": "debe ser una direccion de correo valida"}
              ]
            }
            """;

    public static final String NO_ENCONTRADO = """
            {
              "timestamp": "2026-06-10T15:30:00Z",
              "status": 404,
              "error": "Not Found",
              "codigo": "MEDICO_NO_ENCONTRADO",
              "mensaje": "No existe el medico solicitado",
              "path": "/api/v1/medicos/00000000-0000-0000-0000-000000000099/disponibilidad"
            }
            """;

    public static final String CONFLICTO = """
            {
              "timestamp": "2026-06-10T15:30:00Z",
              "status": 409,
              "error": "Conflict",
              "codigo": "HORARIO_OCUPADO",
              "mensaje": "El medico ya tiene una cita programada en esa franja",
              "path": "/api/v1/citas"
            }
            """;

    public static final String DOCUMENTO_DUPLICADO = """
            {
              "timestamp": "2026-06-10T15:30:00Z",
              "status": 409,
              "error": "Conflict",
              "codigo": "DOCUMENTO_DUPLICADO",
              "mensaje": "Ya existe un paciente con ese documento",
              "path": "/api/v1/pacientes"
            }
            """;

    public static final String TIPO_CONTENIDO_NO_SOPORTADO = """
            {
              "timestamp": "2026-06-10T15:30:00Z",
              "status": 415,
              "error": "Unsupported Media Type",
              "codigo": "TIPO_CONTENIDO_NO_SOPORTADO",
              "mensaje": "El Content-Type de la solicitud no esta soportado",
              "path": "/api/v1/citas"
            }
            """;

    public static final String ERROR_INTERNO = """
            {
              "timestamp": "2026-06-10T15:30:00Z",
              "status": 500,
              "error": "Internal Server Error",
              "codigo": "ERROR_INTERNO",
              "mensaje": "Ocurrio un error interno",
              "path": "/api/v1/citas"
            }
            """;

    private OpenApiExamples() {
    }
}
