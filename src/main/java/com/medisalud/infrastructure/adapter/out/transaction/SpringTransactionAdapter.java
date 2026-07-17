package com.medisalud.infrastructure.adapter.out.transaction;

import com.medisalud.application.port.TransaccionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class SpringTransactionAdapter implements TransaccionPort {

    private final TransactionTemplate transactionTemplate;

    public SpringTransactionAdapter(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <T> T ejecutar(Supplier<T> operacion) {
        return transactionTemplate.execute(status -> operacion.get());
    }
}
