package com.example.healthcare.service.strategy.appointment;

import com.example.healthcare.entity.Appointment;
import com.example.healthcare.entity.AppointmentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompletedAppointmentStrategyTest {

    private final CompletedAppointmentStrategy strategy = new CompletedAppointmentStrategy();

    @Test
    void getSupportedStatus() {
        assertEquals(AppointmentStatus.COMPLETED, strategy.getSupportedStatus());
    }

    @Test
    void handleStatusChange() {
        Appointment appt = new Appointment();
        appt.setId(1L);

        // This method only logs currently
        assertDoesNotThrow(() -> strategy.handleStatusChange(appt));
    }
}
