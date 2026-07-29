package com.example.healthcare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    @NotNull(message = "L'appuntamento è obbligatorio")
    private Appointment appointment;

    // File details
    private String fileName;
    private String fileType;
    private String filePath;

    // AI Agent Extracted Data (stored as JSON string)
    @Column(columnDefinition = "TEXT")
    private String extractedData;

    // Doctor's final notes after reviewing
    @Column(columnDefinition = "TEXT")
    private String doctorNotes;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
