package com.example.healthcare.controller;

import com.example.healthcare.dto.AppointmentDTO;
import com.example.healthcare.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'SUPPORT')")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'SUPPORT') or @ownership.isSelfPatient(#patientId, authentication)")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(patientId));
    }

    // Un paziente prenota solo per sé; Medico/Supporto possono fissare un
    // appuntamento per conto di un paziente (es. follow-up, walk-in).
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'SUPPORT') or (hasRole('PATIENT') and @ownership.isSelfPatient(#appointmentDTO.patientId(), authentication))")
    public ResponseEntity<AppointmentDTO> createAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO) {
        return new ResponseEntity<>(appointmentService.createAppointment(appointmentDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ownership.canEditAppointment(#id, authentication)")
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @PathVariable Long id,
            @RequestBody AppointmentDTO appointmentDTO) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, appointmentDTO));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@ownership.canManageAppointmentStatus(#id, #status, authentication)")
    public ResponseEntity<AppointmentDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'SUPPORT')")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
