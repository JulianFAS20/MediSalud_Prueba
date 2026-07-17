package com.medisalud.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginacionTest {

    @Test
    void debeCrearUnaSolicitudDePaginaValida() {
        assertThat(new Paginacion(2, 25))
                .isEqualTo(new Paginacion(2, 25));
    }

    @Test
    void debeRechazarSolicitudDePaginaInvalida() {
        assertThatThrownBy(() -> new Paginacion(-1, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede ser negativo");
        assertThatThrownBy(() -> new Paginacion(0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor que cero");
    }

    @Test
    void debeMapearContenidoSinPerderMetadatos() {
        List<String> origenMutable = new ArrayList<>(List.of("1", "2"));
        Pagina<String> pagina = new Pagina<>(origenMutable, 1, 2, 5, 3);
        origenMutable.add("3");

        Pagina<Integer> mapeada = pagina.map(Integer::valueOf);

        assertThat(pagina.contenido()).containsExactly("1", "2");
        assertThat(mapeada.contenido()).containsExactly(1, 2);
        assertThat(mapeada.pagina()).isEqualTo(1);
        assertThat(mapeada.tamanio()).isEqualTo(2);
        assertThat(mapeada.totalElementos()).isEqualTo(5);
        assertThat(mapeada.totalPaginas()).isEqualTo(3);
    }

    @Test
    void debeIdentificarPrimeraUltimaYPaginaIntermedia() {
        Pagina<String> vacia = new Pagina<>(List.of(), 0, 20, 0, 0);
        Pagina<String> intermedia = new Pagina<>(List.of("cita"), 1, 1, 3, 3);
        Pagina<String> ultima = new Pagina<>(List.of("cita"), 2, 1, 3, 3);

        assertThat(vacia.primera()).isTrue();
        assertThat(vacia.ultima()).isTrue();
        assertThat(intermedia.primera()).isFalse();
        assertThat(intermedia.ultima()).isFalse();
        assertThat(ultima.ultima()).isTrue();
    }

    @Test
    void debeRechazarMetadatosInvalidos() {
        assertThatThrownBy(() -> new Pagina<>(List.of(), -1, 20, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Pagina<>(List.of(), 0, 0, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Pagina<>(List.of(), 0, 20, -1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Pagina<>(List.of(), 0, 20, 0, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
