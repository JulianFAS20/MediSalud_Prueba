package com.medisalud.application.port;

import java.util.function.Supplier;

public interface TransaccionPort {

    <T> T ejecutar(Supplier<T> operacion);
}
