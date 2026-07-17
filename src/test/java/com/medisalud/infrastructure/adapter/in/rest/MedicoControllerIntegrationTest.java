package com.medisalud.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MedicoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registraMedicoYDevuelveElContratoRestPropio() throws Exception {
        String request = objectMapper.writeValueAsString(Map.of(
                "nombreCompleto", "Dra. Valentina Rojas",
                "especialidad", "Neurologia",
                "telefono", "555-2030",
                "email", "valentina.rojas@medisalud.com"));

        String body = mockMvc.perform(post("/api/v1/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.nombreCompleto").value("Dra. Valentina Rojas"))
                .andExpect(jsonPath("$.especialidad").value("Neurologia"))
                .andExpect(jsonPath("$.telefono").value("555-2030"))
                .andExpect(jsonPath("$.email").value("valentina.rojas@medisalud.com"))
                .andReturn().getResponse().getContentAsString();

        JsonNode response = objectMapper.readTree(body);
        assertThatCode(() -> UUID.fromString(response.get("id").asText())).doesNotThrowAnyException();
        assertThat(response.properties()).extracting(Map.Entry::getKey)
                .containsExactlyInAnyOrder("id", "nombreCompleto", "especialidad", "telefono", "email");
    }
}
