package com.example.healthcare.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MedicalReportResponse(
    Long id,
    Long appointmentId,
    String doctorName,
    String patientName,
    LocalDateTime appointmentDate,
    
    // File info
    String fileName,
    String fileType,
    String downloadUrl,

    // AI Agent Extracted Data & Notes
    String extractedData,
    String doctorNotes,

    LocalDateTime createdAt
) {}
