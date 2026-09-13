package com.example.healthcare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

/** Genera più slot consecutivi della stessa durata in un'unica finestra oraria. */
@Builder
public record SlotBatchRequest(
    @NotNull(message = "L'ID del medico è obbligatorio")
    Long doctorId,

    @NotNull(message = "La data è obbligatoria")
    LocalDate date,

    @NotNull(message = "L'ora di inizio è obbligatoria")
    LocalTime startTime,

    @NotNull(message = "L'ora di fine è obbligatoria")
    LocalTime endTime,

    @NotNull(message = "La durata dello slot è obbligatoria")
    @Min(value = 5, message = "La durata dello slot deve essere di almeno 5 minuti")
    Integer slotDurationMinutes
) {}
