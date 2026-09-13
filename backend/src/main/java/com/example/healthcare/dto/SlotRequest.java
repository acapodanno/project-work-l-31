package com.example.healthcare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record SlotRequest(
    @NotNull(message = "L'ID del medico è obbligatorio")
    Long doctorId,

    @NotNull(message = "La data è obbligatoria")
    LocalDate date,

    @NotNull(message = "L'ora di inizio è obbligatoria")
    LocalTime startTime,

    @NotNull(message = "L'ora di fine è obbligatoria")
    LocalTime endTime
) {}
