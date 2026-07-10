package com.example.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDTO {
    private Long id;

    @NotNull(message = "L'ID del paziente è obbligatorio")
    private Long patientId;

    @NotBlank(message = "Il titolo del ticket è obbligatorio")
    private String title;

    @NotBlank(message = "La descrizione è obbligatoria")
    private String description;

    private String status;

    private LocalDateTime createdAt;

    // Per mostrare i dettagli del paziente se necessario
    private PatientDTO patient;
}
