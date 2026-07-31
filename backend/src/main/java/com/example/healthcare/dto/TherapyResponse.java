package com.example.healthcare.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record TherapyResponse(
    Long id,
    PatientDTO patient,
    DoctorDTO doctor,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    LocalDateTime createdAt
) {}
