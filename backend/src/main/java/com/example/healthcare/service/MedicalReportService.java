package com.example.healthcare.service;

import com.example.healthcare.dto.MedicalReportResponse;
import com.example.healthcare.entity.Appointment;
import com.example.healthcare.entity.AppointmentStatus;
import com.example.healthcare.entity.MedicalReport;
import com.example.healthcare.repository.AppointmentRepository;
import com.example.healthcare.repository.MedicalReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalReportService {

    private final MedicalReportRepository medicalReportRepository;
    private final AppointmentRepository appointmentRepository;
    private final FileStorageService fileStorageService;
    private final DataExtractionAgent dataExtractionAgent;

    @Transactional
    public MedicalReportResponse uploadReport(Long appointmentId, MultipartFile file, String patientEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appuntamento non trovato"));

        if (!appointment.getPatient().getEmail().equals(patientEmail)) {
            throw new RuntimeException("Non autorizzato a caricare referti per questo appuntamento");
        }

        if (medicalReportRepository.findByAppointmentId(appointment.getId()).isPresent()) {
            throw new RuntimeException("Esiste già un documento per questo appuntamento");
        }

        // 1. Salva il file
        String fileName = fileStorageService.storeFile(file);

        // 2. Chiama l'Agente IA per l'estrazione dati (simulata)
        String extractedData = dataExtractionAgent.extractData(file.getOriginalFilename());

        MedicalReport report = MedicalReport.builder()
                .appointment(appointment)
                .fileName(fileName)
                .fileType(file.getContentType())
                .filePath("uploads/" + fileName)
                .extractedData(extractedData)
                .build();

        MedicalReport savedReport = medicalReportRepository.save(report);
        return mapToResponse(savedReport);
    }

    @Transactional
    public MedicalReportResponse addDoctorNotes(Long reportId, String notes, String doctorEmail) {
        MedicalReport report = medicalReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Referto non trovato"));
                
        if (!report.getAppointment().getDoctor().getEmail().equals(doctorEmail)) {
            throw new RuntimeException("Non autorizzato ad aggiungere note a questo referto");
        }

        report.setDoctorNotes(notes);
        
        // Segna l'appuntamento come completato quando il medico aggiunge le note finali
        Appointment appointment = report.getAppointment();
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        return mapToResponse(medicalReportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public MedicalReportResponse getReportByAppointmentId(Long appointmentId) {
        MedicalReport report = medicalReportRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Referto non trovato"));
        return mapToResponse(report);
    }

    @Transactional(readOnly = true)
    public List<MedicalReportResponse> getReportsByPatientId(Long patientId) {
        return medicalReportRepository.findByAppointmentPatientId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private MedicalReportResponse mapToResponse(MedicalReport report) {
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/reports/download/")
                .path(report.getFileName())
                .toUriString();

        return MedicalReportResponse.builder()
                .id(report.getId())
                .appointmentId(report.getAppointment().getId())
                .doctorName(report.getAppointment().getDoctor().getName())
                .patientName(report.getAppointment().getPatient().getName())
                .appointmentDate(report.getAppointment().getAppointmentDate())
                .fileName(report.getFileName())
                .fileType(report.getFileType())
                .downloadUrl(fileDownloadUri)
                .extractedData(report.getExtractedData())
                .doctorNotes(report.getDoctorNotes())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
