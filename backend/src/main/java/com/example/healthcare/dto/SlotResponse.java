package com.example.healthcare.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record SlotResponse(
    Long id,
    Long doctorId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    boolean booked
) {}
