package com.medisalud.infrastructure.adapter.in.rest.response;

import com.medisalud.application.dto.CitaDto;
import com.medisalud.domain.model.EstadoCita;
import com.medisalud.domain.model.Pagina;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaginaCitasResponseTest {

    @Test
    void debeConvertirContenidoYMetadatos() {
        Instant inicio = Instant.parse("2030-01-16T13:00:00Z");
        CitaDto cita = new CitaDto(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                inicio, inicio.plusSeconds(1800), EstadoCita.PROGRAMADA, null);
        Pagina<CitaDto> pagina = new Pagina<>(List.of(cita), 0, 1, 2, 2);

        PaginaCitasResponse response = PaginaCitasResponse.desde(
                pagina, ZoneId.of("America/Bogota"));

        assertThat(response.contenido()).singleElement().satisfies(item ->
                assertThat(item.fechaHora().toInstant()).isEqualTo(inicio));
        assertThat(response.pagina()).isZero();
        assertThat(response.tamanio()).isEqualTo(1);
        assertThat(response.totalElementos()).isEqualTo(2);
        assertThat(response.totalPaginas()).isEqualTo(2);
        assertThat(response.primera()).isTrue();
        assertThat(response.ultima()).isFalse();
    }
}
