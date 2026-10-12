package com.atlas.bank.infrastructure.adapter.listener;

import com.atlas.bank.domain.event.TransactionExecutedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationListener {

    @EventListener
    public void onTransactionExecuted( TransactionExecutedEvent event ) {
        log.info( "Enviando comprobante de {} por ${} - Transacción #{}",
                event.type(), event.amount(), event.transactionId() );
    }

}