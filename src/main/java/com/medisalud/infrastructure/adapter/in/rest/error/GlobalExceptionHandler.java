package com.medisalud.infrastructure.adapter.in.rest.error;

import com.medisalud.domain.exception.ConflictException;
import com.medisalud.domain.exception.DomainException;
import com.medisalud.domain.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Clock;
import java.util.List;

@RestControllerAdvice
@SuppressWarnings("unused") // Spring invoca los metodos @ExceptionHandler mediante reflexion.
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock reloj;

    public GlobalExceptionHandler(Clock reloj) {
        this.reloj = reloj;
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiError> manejarDominio(DomainException exception, HttpServletRequest request) {
        HttpStatus status = exception instanceof NotFoundException
                ? HttpStatus.NOT_FOUND
                : exception instanceof ConflictException ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return respuesta(status, exception.codigo(), exception.getMessage(), request.getRequestURI(), List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException exception,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        List<CampoError> errores = exception.getBindingResult().getFieldErrors().stream()
                .map(this::aCampoError)
                .toList();
        return respuestaMvc(status, "REQUEST_INVALIDO", "La solicitud contiene campos invalidos", request, errores);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            @NonNull HandlerMethodValidationException exception,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        List<CampoError> errores = exception.getAllErrors().stream()
                .map(error -> new CampoError("parametro", error.getDefaultMessage()))
                .toList();
        return respuestaMvc(status, "REQUEST_INVALIDO", "La solicitud contiene parametros invalidos",
                request, errores);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException exception,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return respuestaMvc(status, "JSON_INVALIDO", "El cuerpo de la solicitud no es un JSON valido",
                request, List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @NonNull MissingServletRequestParameterException exception,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        CampoError error = new CampoError(exception.getParameterName(), "El parametro es obligatorio");
        return respuestaMvc(status, "PARAMETRO_REQUERIDO", "Falta un parametro obligatorio",
                request, List.of(error));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            @NonNull TypeMismatchException exception,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        String campo = exception.getPropertyName() == null ? "parametro" : exception.getPropertyName();
        CampoError error = new CampoError(campo, "El valor no tiene el tipo o formato esperado");
        return respuestaMvc(status, "PARAMETRO_INVALIDO", "La solicitud contiene un parametro invalido",
                request, List.of(error));
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            @NonNull NoResourceFoundException exception,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return respuestaMvc(status, "RUTA_NO_ENCONTRADA", "La ruta solicitada no existe",
                request, List.of());
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            @NonNull HttpRequestMethodNotSupportedException exception,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return respuestaMvc(status, "METODO_NO_PERMITIDO", "El metodo HTTP no esta permitido para esta ruta",
                request, List.of());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            @NonNull HttpMediaTypeNotSupportedException exception,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return respuestaMvc(status, "TIPO_CONTENIDO_NO_SOPORTADO",
                "El Content-Type de la solicitud no esta soportado", request, List.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> manejarRestricciones(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        List<CampoError> errores = exception.getConstraintViolations().stream()
                .map(violacion -> new CampoError(violacion.getPropertyPath().toString(), violacion.getMessage()))
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "REQUEST_INVALIDO",
                "La solicitud contiene parametros invalidos", request.getRequestURI(), errores);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
    ResponseEntity<ApiError> manejarConflictoPersistencia(Exception exception, HttpServletRequest request) {
        LOG.warn("Conflicto de persistencia en {}", request.getRequestURI());
        return respuesta(HttpStatus.CONFLICT, "CONFLICTO_CONCURRENCIA",
                "La operacion entra en conflicto con datos existentes; consulte disponibilidad e intente de nuevo",
                request.getRequestURI(), List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> manejarInesperado(Exception exception, HttpServletRequest request) {
        LOG.error("Error no controlado en {}", request.getRequestURI(), exception);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrio un error interno", request.getRequestURI(), List.of());
    }

    private CampoError aCampoError(FieldError error) {
        return new CampoError(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ApiError> respuesta(HttpStatus status, String codigo, String mensaje,
                                                String path, List<CampoError> errores) {
        ApiError error = crearError(status, codigo, mensaje, path, errores);
        return ResponseEntity.status(status).body(error);
    }

    private ResponseEntity<Object> respuestaMvc(HttpStatusCode statusCode, String codigo, String mensaje,
                                                 WebRequest request, List<CampoError> errores) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        ApiError error = crearError(status, codigo, mensaje, obtenerPath(request), errores);
        return ResponseEntity.status(statusCode).body(error);
    }

    private ApiError crearError(HttpStatus status, String codigo, String mensaje,
                                String path, List<CampoError> errores) {
        return new ApiError(reloj.instant(), status.value(), status.getReasonPhrase(), codigo,
                mensaje, path, errores.isEmpty() ? null : errores);
    }

    private String obtenerPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return request.getDescription(false).replace("uri=", "");
    }
}
