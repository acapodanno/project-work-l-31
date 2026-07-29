package com.example.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AppointmentDTO(
    Long id,

    @NotNull(message = "L'ID del paziente è obbligatorio")
    Long patientId,

    @NotNull(message = "L'ID del medico è obbligatorio")
    Long doctorId,

    @NotNull(message = "La data dell'appuntamento è obbligatoria")
    LocalDateTime appointmentDate,

    @NotBlank(message = "Il motivo della visita è obbligatorio")
    String reason,

    String notes,

    String status,

    // Campi di sola lettura per la visualizzazione dei dettagli nel frontend
    PatientDTO patient,
    DoctorDTO doctor
) {}
