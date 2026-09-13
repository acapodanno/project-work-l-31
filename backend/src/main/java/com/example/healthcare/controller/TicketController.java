package com.example.healthcare.controller;

import com.example.healthcare.dto.TicketDTO;
import com.example.healthcare.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    @PreAuthorize("hasRole('SUPPORT')")
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('SUPPORT') or @ownership.isSelfPatient(#patientId, authentication)")
    public ResponseEntity<List<TicketDTO>> getTicketsByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(ticketService.getTicketsByPatientId(patientId));
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT') and @ownership.isSelfPatient(#ticketDTO.patientId(), authentication)")
    public ResponseEntity<TicketDTO> createTicket(@Valid @RequestBody TicketDTO ticketDTO) {
        return new ResponseEntity<>(ticketService.createTicket(ticketDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPPORT')")
    public ResponseEntity<TicketDTO> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketDTO ticketDTO) {
        return ResponseEntity.ok(ticketService.updateTicket(id, ticketDTO));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPPORT')")
    public ResponseEntity<TicketDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ticketService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPPORT')")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
