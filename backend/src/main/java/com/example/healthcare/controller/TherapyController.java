package com.example.healthcare.controller;

import com.example.healthcare.dto.TherapyRequest;
import com.example.healthcare.dto.TherapyResponse;
import com.example.healthcare.service.TherapyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/therapies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TherapyController {

    private final TherapyService therapyService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') and @ownership.isSelfDoctor(#request.doctorId(), authentication)")
    public ResponseEntity<TherapyResponse> createTherapy(@Valid @RequestBody TherapyRequest request) {
        return ResponseEntity.ok(therapyService.createTherapy(request));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'SUPPORT') or @ownership.isSelfPatient(#patientId, authentication)")
    public ResponseEntity<List<TherapyResponse>> getTherapiesByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(therapyService.getTherapiesByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('SUPPORT') or @ownership.isSelfDoctor(#doctorId, authentication)")
    public ResponseEntity<List<TherapyResponse>> getTherapiesByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(therapyService.getTherapiesByDoctor(doctorId));
    }
}
