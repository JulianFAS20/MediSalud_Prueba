package com.medisalud.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicaSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }

    @Test
    void generaContratoOpenApiConResponsesRestYErroresEjemplificados() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", startsWith("3.")))
                .andExpect(jsonPath("$.info.title").value("MediSalud API"))
                .andExpect(jsonPath("$['components']['schemas']['MedicoResponse']").exists())
                .andExpect(jsonPath("$['components']['schemas']['PacienteResponse']").exists())
                .andExpect(jsonPath("$['components']['schemas']['MedicoDto']").doesNotExist())
                .andExpect(jsonPath("$['components']['schemas']['PacienteDto']").doesNotExist())
                .andExpect(jsonPath("$['components']['schemas']['RegistrarMedicoRequest']"
                        + "['properties']['nombreCompleto']['example']").value("Dra. Maria Gonzalez"))
                .andExpect(jsonPath("$['paths']['/api/v1/medicos']['post']['operationId']")
                        .value("registrarMedico"))
                .andExpect(jsonPath("$['paths']['/api/v1/pacientes']['post']['operationId']")
                        .value("registrarPaciente"))
                .andExpect(jsonPath("$['paths']['/api/v1/medicos']['post']['responses']['201']"
                        + "['content']['application/json']['schema']['$ref']",
                        startsWith("#/components/schemas/MedicoResponse")))
                .andExpect(jsonPath("$['paths']['/api/v1/pacientes']['post']['responses']['409']"
                        + "['content']['application/json']['example']['codigo']").value("DOCUMENTO_DUPLICADO"))
                .andExpect(jsonPath("$['paths']['/api/v1/citas']['post']['responses']['400']"
                        + "['content']['application/json']['example']['codigo']").value("REQUEST_INVALIDO"))
                .andExpect(jsonPath("$['paths']['/api/v1/citas']['post']['responses']['500']"
                        + "['content']['application/json']['schema']['$ref']",
                        startsWith("#/components/schemas/ApiError")));
    }
}
