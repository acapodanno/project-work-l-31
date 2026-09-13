package com.example.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TherapyRequest(
    @NotNull(message = "L'ID del paziente è obbligatorio")
    Long patientId,

    @NotNull(message = "L'ID del medico è obbligatorio")
    Long doctorId,

    @NotBlank(message = "La descrizione della terapia è obbligatoria")
    String description,

    @NotNull(message = "La data di inizio è obbligatoria")
    LocalDate startDate,

    @NotNull(message = "La data di fine è obbligatoria")
    LocalDate endDate,

    // Facoltativo: valorizzato quando la terapia viene prescritta durante una visita specifica.
    Long appointmentId
) {}
