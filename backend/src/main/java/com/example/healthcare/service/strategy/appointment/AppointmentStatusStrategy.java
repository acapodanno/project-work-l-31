package com.example.healthcare.service.strategy.appointment;

import com.example.healthcare.entity.Appointment;
import com.example.healthcare.entity.AppointmentStatus;

public interface AppointmentStatusStrategy {
    void handleStatusChange(Appointment appointment);

    AppointmentStatus getSupportedStatus();
}
