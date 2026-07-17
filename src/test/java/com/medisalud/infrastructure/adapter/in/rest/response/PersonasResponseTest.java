package com.medisalud.infrastructure.adapter.in.rest.response;

import com.medisalud.application.dto.MedicoDto;
import com.medisalud.application.dto.PacienteDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PersonasResponseTest {

    @Test
    void mapeaMedicoDtoAlContratoRest() {
        UUID id = UUID.randomUUID();
        MedicoDto medico = new MedicoDto(id, "Dra. Maria Gonzalez", "Cardiologia",
                "555-1001", "maria.gonzalez@medisalud.com");

        MedicoResponse response = MedicoResponse.desde(medico);

        assertThat(response).isEqualTo(new MedicoResponse(id, "Dra. Maria Gonzalez", "Cardiologia",
                "555-1001", "maria.gonzalez@medisalud.com"));
    }

    @Test
    void mapeaPacienteDtoAlContratoRest() {
        UUID id = UUID.randomUUID();
        LocalDate nacimiento = LocalDate.of(1992, 5, 18);
        PacienteDto paciente = new PacienteDto(id, "Laura Martinez", "CC-1032456789",
                "3001234567", "laura.martinez@example.com", nacimiento);

        PacienteResponse response = PacienteResponse.desde(paciente);

        assertThat(response).isEqualTo(new PacienteResponse(id, "Laura Martinez", "CC-1032456789",
                "3001234567", "laura.martinez@example.com", nacimiento));
    }

    @Test
    void rechazaDtosNulosEnElBordeRest() {
        assertThatNullPointerException().isThrownBy(() -> MedicoResponse.desde(null))
                .withMessage("El medico es obligatorio");
        assertThatNullPointerException().isThrownBy(() -> PacienteResponse.desde(null))
                .withMessage("El paciente es obligatorio");
    }
}
