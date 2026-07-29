package com.example.healthcare.service.strategy.ticket;

import com.example.healthcare.entity.Ticket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClosedTicketStrategyTest {

    private final ClosedTicketStrategy strategy = new ClosedTicketStrategy();

    @Test
    void getSupportedStatus() {
        assertEquals("CLOSED", strategy.getSupportedStatus());
    }

    @Test
    void handleStatusChange() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);

        // This method only logs currently
        assertDoesNotThrow(() -> strategy.handleStatusChange(ticket));
    }
}
