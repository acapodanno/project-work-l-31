package com.example.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TicketDTO(
    Long id,

    @NotNull(message = "L'ID del paziente è obbligatorio")
    Long patientId,

    @NotBlank(message = "Il titolo del ticket è obbligatorio")
    String title,

    @NotBlank(message = "La descrizione è obbligatoria")
    String description,

    String status,

    LocalDateTime createdAt,

    // Per mostrare i dettagli del paziente se necessario
    PatientDTO patient
) {}
