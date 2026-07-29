package com.example.healthcare.controller;

import com.example.healthcare.dto.TherapyRequest;
import com.example.healthcare.entity.Therapy;
import com.example.healthcare.service.TherapyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/therapies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TherapyController {

    private final TherapyService therapyService;

    @PostMapping
    public ResponseEntity<Therapy> createTherapy(@RequestBody TherapyRequest request) {
        return ResponseEntity.ok(therapyService.createTherapy(request));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Therapy>> getTherapiesByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(therapyService.getTherapiesByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Therapy>> getTherapiesByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(therapyService.getTherapiesByDoctor(doctorId));
    }
}
