package com.example.healthcare.service.strategy.appointment;

import com.example.healthcare.entity.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppointmentStrategyFactoryTest {

    private AppointmentStrategyFactory factory;
    private AppointmentStatusStrategy cancelledStrategy;
    private AppointmentStatusStrategy completedStrategy;

    @BeforeEach
    void setUp() {
        cancelledStrategy = mock(AppointmentStatusStrategy.class);
        when(cancelledStrategy.getSupportedStatus()).thenReturn(AppointmentStatus.CANCELLED);

        completedStrategy = mock(AppointmentStatusStrategy.class);
        when(completedStrategy.getSupportedStatus()).thenReturn(AppointmentStatus.COMPLETED);

        factory = new AppointmentStrategyFactory(List.of(cancelledStrategy, completedStrategy));
    }

    @Test
    void getStrategy_Cancelled() {
        AppointmentStatusStrategy result = factory.getStrategy(AppointmentStatus.CANCELLED);
        assertEquals(cancelledStrategy, result);
    }

    @Test
    void getStrategy_Completed() {
        AppointmentStatusStrategy result = factory.getStrategy(AppointmentStatus.COMPLETED);
        assertEquals(completedStrategy, result);
    }

    @Test
    void getStrategy_NotFound() {
        AppointmentStatusStrategy result = factory.getStrategy(AppointmentStatus.SCHEDULED);
        assertNull(result);
    }
}
