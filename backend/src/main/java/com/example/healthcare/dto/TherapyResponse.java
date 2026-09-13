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
    LocalDateTime createdAt,

    // Riferimento alla visita che ha generato la terapia, se presente. Inclusi qui direttamente
    // (invece di rimandare a un'altra chiamata) perché sono gli unici dati dell'appuntamento che
    // servono per mostrarne il collegamento nell'interfaccia.
    Long appointmentId,
    LocalDateTime appointmentDate,
    String appointmentReason
) {}
