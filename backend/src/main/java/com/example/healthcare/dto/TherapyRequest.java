package com.example.healthcare.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TherapyRequest(
    Long patientId,
    Long doctorId,
    String description,
    LocalDate startDate,
    LocalDate endDate
) {}
