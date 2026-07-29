package com.example.healthcare.service.strategy.ticket;

import com.example.healthcare.entity.Ticket;

public interface TicketStatusStrategy {
    void handleStatusChange(Ticket ticket);

    String getSupportedStatus();
}
