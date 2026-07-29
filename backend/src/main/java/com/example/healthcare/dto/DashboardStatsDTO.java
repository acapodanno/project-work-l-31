package com.example.healthcare.dto;

import lombok.Builder;

@Builder
public record DashboardStatsDTO(
    long totalPatients,
    long totalDoctors,
    long openTickets,
    long completedAppointments,
    long scheduledAppointments
) {}
