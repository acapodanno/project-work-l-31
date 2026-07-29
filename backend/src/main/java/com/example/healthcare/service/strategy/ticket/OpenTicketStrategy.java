package com.example.healthcare.service.strategy.ticket;

import com.example.healthcare.entity.Ticket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OpenTicketStrategy implements TicketStatusStrategy {

    @Override
    public void handleStatusChange(Ticket ticket) {
        log.info("Il ticket {} è stato (ri)aperto. Logica di riassegnazione o notifica admin in corso...",
                ticket.getId());
    }

    @Override
    public String getSupportedStatus() {
        return "OPEN";
    }
}
