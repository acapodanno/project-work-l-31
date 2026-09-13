package com.example.healthcare.dto;

import lombok.Builder;

import java.time.LocalTime;

/** Finestra oraria che la generazione batch non ha creato perché si sovrapponeva a uno slot già dichiarato. */
@Builder
public record SkippedSlotRange(
    LocalTime startTime,
    LocalTime endTime
) {}
