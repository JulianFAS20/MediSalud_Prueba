package com.medisalud.infrastructure.adapter.in.rest.error;

import com.medisalud.domain.exception.ValidationException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandlerIntegrationTest.ErrorProbeController.class)
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void debeMapearExcepcionDominioSegunSuTipo() throws Exception {
        mockMvc.perform(patch("/api/v1/citas/{id}/cancelacion",
                        "11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CITA_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/citas/11111111-1111-1111-1111-111111111111/cancelacion"));

        mockMvc.perform(get("/__test/errores/dominio"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REGLA_INVALIDA"));
    }

    @Test
    void debeMapearErroresDeBeanValidationConDetalleDeCampos() throws Exception {
        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUEST_INVALIDO"))
                .andExpect(jsonPath("$.erroresCampo").isArray())
                .andExpect(jsonPath("$.erroresCampo[?(@.campo == 'nombreCompleto')]").exists())
                .andExpect(jsonPath("$.erroresCampo[?(@.campo == 'documentoIdentidad')]").exists());
    }

    @Test
    void debeMapearValidacionDeParametrosDeMetodo() throws Exception {
        mockMvc.perform(get("/__test/errores/validacion-metodo").param("cantidad", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUEST_INVALIDO"))
                .andExpect(jsonPath("$.erroresCampo").isArray());
    }

    @Test
    void debeMapearJsonIlegible() throws Exception {
        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("JSON_INVALIDO"));
    }

    @Test
    void debeMapearParametroObligatorioAusente() throws Exception {
        mockMvc.perform(get("/api/v1/medicos/{id}/disponibilidad",
                        "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PARAMETRO_REQUERIDO"))
                .andExpect(jsonPath("$.erroresCampo[0].campo").value("fechaInicio"));
    }

    @Test
    void debeMapearParametroConTipoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/citas").param("estado", "DESCONOCIDA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"))
                .andExpect(jsonPath("$.erroresCampo[0].campo").value("estado"));
    }

    @Test
    void debeConservar404ParaRutaInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/ruta-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RUTA_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.path").value("/api/v1/ruta-inexistente"));
    }

    @Test
    void debeConservar405ParaMetodoNoPermitido() throws Exception {
        mockMvc.perform(put("/api/v1/pacientes"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value("METODO_NO_PERMITIDO"));
    }

    @Test
    void debeConservar415ParaContentTypeNoSoportado() throws Exception {
        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("contenido no JSON"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo").value("TIPO_CONTENIDO_NO_SOPORTADO"));
    }

    @Test
    void debeMapearConstraintViolation() throws Exception {
        mockMvc.perform(get("/__test/errores/restriccion"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUEST_INVALIDO"));
    }

    @Test
    void debeMapearConflictoDePersistencia() throws Exception {
        mockMvc.perform(get("/__test/errores/persistencia"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_CONCURRENCIA"));
    }

    @Test
    void debeOcultarDetallesDeErroresInesperados() throws Exception {
        mockMvc.perform(get("/__test/errores/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.mensaje").value("Ocurrio un error interno"));
    }

    @RestController
    @RequestMapping("/__test/errores")
    static class ErrorProbeController {

        @GetMapping("/dominio")
        void dominio() {
            throw new ValidationException("REGLA_INVALIDA", "La regla no se cumple");
        }

        @GetMapping("/validacion-metodo")
        int validacionMetodo(@RequestParam @Min(1) int cantidad) {
            return cantidad;
        }

        @GetMapping("/restriccion")
        void restriccion() {
            throw new ConstraintViolationException("Restriccion de prueba", Set.of());
        }

        @GetMapping("/persistencia")
        void persistencia() {
            throw new DataIntegrityViolationException("Detalle sensible de base de datos");
        }

        @GetMapping("/inesperado")
        void inesperado() {
            throw new IllegalStateException("Detalle sensible que no debe exponerse");
        }
    }
}
