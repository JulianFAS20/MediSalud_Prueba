package com.medisalud.infrastructure.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI medisaludOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediSalud API")
                        .version("v1")
                        .description("API REST para el agendamiento, disponibilidad, cancelacion y "
                                + "reprogramacion de citas medicas.")
                        .contact(new Contact().name("MediSalud")))
                .externalDocs(new ExternalDocumentation()
                        .description("Repositorio y guia de ejecucion")
                        .url("https://github.com/JulianFAS20/MediSalud_Prueba"));
    }
}
