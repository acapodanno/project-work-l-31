package com.example.healthcare.controller;

import com.example.healthcare.dto.MedicalReportResponse;
import com.example.healthcare.service.FileStorageService;
import com.example.healthcare.service.MedicalReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class MedicalReportController {

    private final MedicalReportService medicalReportService;
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<MedicalReportResponse> uploadReport(
            @RequestParam("appointmentId") Long appointmentId,
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        String patientEmail = principal.getName();
        return ResponseEntity.ok(medicalReportService.uploadReport(appointmentId, file, patientEmail));
    }

    @PutMapping("/{id}/notes")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalReportResponse> addDoctorNotes(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Principal principal) {
        String doctorEmail = principal.getName();
        return ResponseEntity.ok(medicalReportService.addDoctorNotes(id, payload.get("notes"), doctorEmail));
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('SUPPORT') or @ownership.canAccessReport(#appointmentId, authentication)")
    public ResponseEntity<MedicalReportResponse> getReportByAppointmentId(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(medicalReportService.getReportByAppointmentId(appointmentId));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT') and @ownership.isSelfPatient(#patientId, authentication)")
    public ResponseEntity<List<MedicalReportResponse>> getReportsByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalReportService.getReportsByPatientId(patientId));
    }

    @GetMapping("/download/{fileName:.+}")
    @PreAuthorize("@ownership.canDownloadReport(#fileName, authentication)")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Log fallback
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
