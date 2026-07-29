package com.example.healthcare.service.strategy.appointment;

import com.example.healthcare.entity.Appointment;
import com.example.healthcare.entity.AppointmentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CancelledAppointmentStrategy implements AppointmentStatusStrategy {

    @Override
    public void handleStatusChange(Appointment appointment) {
        log.info("L'appuntamento {} è stato cancellato. Logica di notifica o rimborso in corso...",
                appointment.getId());
        // Logica per liberare lo slot del dottore, mandare email di conferma
        // cancellazione, ecc.
    }

    @Override
    public AppointmentStatus getSupportedStatus() {
        return AppointmentStatus.CANCELLED;
    }
}
