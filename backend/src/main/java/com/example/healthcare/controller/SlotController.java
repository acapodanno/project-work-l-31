package com.example.healthcare.controller;

import com.example.healthcare.dto.NextAvailableSlotResponse;
import com.example.healthcare.dto.SlotBatchRequest;
import com.example.healthcare.dto.SlotBatchResponse;
import com.example.healthcare.dto.SlotRequest;
import com.example.healthcare.dto.SlotResponse;
import com.example.healthcare.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SlotController {

    private final SlotService slotService;

    // Nessuna restrizione di ruolo: il paziente deve vedere gli slot di un
    // medico per prenotare, il medico e il supporto per gestirli.
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<SlotResponse>> getSlotsByDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(slotService.getSlotsByDoctorAndDate(doctorId, date));
    }

    // Nessuna restrizione di ruolo: serve al paziente per confrontare la disponibilità
    // tra più medici prima di aprirne uno, senza doverli interrogare uno alla volta.
    @GetMapping("/next-available")
    public ResponseEntity<List<NextAvailableSlotResponse>> getNextAvailableSlots(
            @RequestParam List<Long> doctorIds,
            @RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(slotService.getNextAvailableSlots(doctorIds, days));
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') and @ownership.isSelfDoctor(#request.doctorId(), authentication)")
    public ResponseEntity<SlotResponse> createSlot(@Valid @RequestBody SlotRequest request) {
        return new ResponseEntity<>(slotService.createSlot(request), HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('DOCTOR') and @ownership.isSelfDoctor(#request.doctorId(), authentication)")
    public ResponseEntity<SlotBatchResponse> createSlotsBatch(@Valid @RequestBody SlotBatchRequest request) {
        return new ResponseEntity<>(slotService.createSlotsBatch(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') and @ownership.isSelfSlotDoctor(#id, authentication)")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
        slotService.deleteSlot(id);
        return ResponseEntity.noContent().build();
    }
}
