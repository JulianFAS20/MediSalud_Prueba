package com.medisalud.infrastructure.adapter.in.rest.error;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String codigo,
        String mensaje,
        String path,
        List<CampoError> erroresCampo) {
}
