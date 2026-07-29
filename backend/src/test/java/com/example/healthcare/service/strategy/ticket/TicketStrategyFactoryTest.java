package com.example.healthcare.service.strategy.ticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TicketStrategyFactoryTest {

    private TicketStrategyFactory factory;
    private TicketStatusStrategy closedStrategy;
    private TicketStatusStrategy openStrategy;

    @BeforeEach
    void setUp() {
        closedStrategy = mock(TicketStatusStrategy.class);
        when(closedStrategy.getSupportedStatus()).thenReturn("CLOSED");

        openStrategy = mock(TicketStatusStrategy.class);
        when(openStrategy.getSupportedStatus()).thenReturn("OPEN");

        factory = new TicketStrategyFactory(List.of(closedStrategy, openStrategy));
    }

    @Test
    void getStrategy_Closed() {
        TicketStatusStrategy result = factory.getStrategy("CLOSED");
        assertEquals(closedStrategy, result);
    }

    @Test
    void getStrategy_Open() {
        TicketStatusStrategy result = factory.getStrategy("open");
        assertEquals(openStrategy, result);
    }

    @Test
    void getStrategy_NotFound() {
        TicketStatusStrategy result = factory.getStrategy("IN_PROGRESS");
        assertNull(result);
    }
}
