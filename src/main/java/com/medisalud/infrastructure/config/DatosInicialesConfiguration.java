package com.medisalud.infrastructure.config;

import com.medisalud.domain.model.Medico;
import com.medisalud.domain.port.MedicoRepositoryPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class DatosInicialesConfiguration {

    public static final UUID MARIA_GONZALEZ_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID CARLOS_RUIZ_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID ANA_LOPEZ_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Bean
    CommandLineRunner cargarMedicosIniciales(MedicoRepositoryPort medicos) {
        return args -> {
            if (medicos.contar() != 0) {
                return;
            }
            medicos.guardar(Medico.restaurar(MARIA_GONZALEZ_ID, "Dra. Maria Gonzalez", "Cardiologia",
                    "555-1001", "maria.gonzalez@medisalud.com"));
            medicos.guardar(Medico.restaurar(CARLOS_RUIZ_ID, "Dr. Carlos Ruiz", "Pediatria",
                    "555-1002", "carlos.ruiz@medisalud.com"));
            medicos.guardar(Medico.restaurar(ANA_LOPEZ_ID, "Dra. Ana Lopez", "Dermatologia",
                    "555-1003", "ana.lopez@medisalud.com"));
        };
    }
}
