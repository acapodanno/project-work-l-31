package com.example.healthcare.service.strategy.appointment;

import com.example.healthcare.entity.Appointment;
import com.example.healthcare.entity.AppointmentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CompletedAppointmentStrategy implements AppointmentStatusStrategy {

    @Override
    public void handleStatusChange(Appointment appointment) {
        log.info("L'appuntamento {} è stato completato. Logica di chiusura post-visita in corso...",
                appointment.getId());
        // Qui si può inserire logica aggiuntiva, es. generare notifica al paziente,
        // triggerare fatturazione, ecc.
    }

    @Override
    public AppointmentStatus getSupportedStatus() {
        return AppointmentStatus.COMPLETED;
    }
}
