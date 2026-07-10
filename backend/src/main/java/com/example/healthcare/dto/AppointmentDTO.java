package com.example.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDTO {
    private Long id;

    @NotNull(message = "L'ID del paziente è obbligatorio")
    private Long patientId;

    @NotNull(message = "L'ID del medico è obbligatorio")
    private Long doctorId;

    @NotNull(message = "La data dell'appuntamento è obbligatoria")
    private LocalDateTime appointmentDate;

    @NotBlank(message = "Il motivo della visita è obbligatorio")
    private String reason;

    private String notes;

    private String status;

    // Campi di sola lettura per la visualizzazione dei dettagli nel frontend
    private PatientDTO patient;
    private DoctorDTO doctor;
}
