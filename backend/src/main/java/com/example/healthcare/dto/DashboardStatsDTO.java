package com.example.healthcare.dto;

import lombok.Builder;

@Builder
public record DashboardStatsDTO(
    long totalPatients,
    long totalDoctors,
    long openTickets,
    long closedTickets,
    long completedAppointments,
    long scheduledAppointments,
    long cancelledAppointments
) {}
