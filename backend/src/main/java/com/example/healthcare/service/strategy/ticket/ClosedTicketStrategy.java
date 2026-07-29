package com.example.healthcare.service.strategy.ticket;

import com.example.healthcare.entity.Ticket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClosedTicketStrategy implements TicketStatusStrategy {

    @Override
    public void handleStatusChange(Ticket ticket) {
        log.info("Il ticket {} è stato chiuso. Logica di notifica al paziente in corso...", ticket.getId());
        // Es: invio email al paziente che il suo ticket è stato risolto
    }

    @Override
    public String getSupportedStatus() {
        return "CLOSED";
    }
}
