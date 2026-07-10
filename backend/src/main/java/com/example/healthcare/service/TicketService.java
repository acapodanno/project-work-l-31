package com.example.healthcare.service;

import com.example.healthcare.dto.TicketDTO;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.entity.Ticket;
import com.example.healthcare.mapper.TicketMapper;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final PatientRepository patientRepository;
    private final TicketMapper ticketMapper;

    @Transactional(readOnly = true)
    public List<TicketDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsByPatientId(Long patientId) {
        return ticketRepository.findByPatientId(patientId).stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketDTO createTicket(TicketDTO ticketDTO) {
        Patient patient = patientRepository.findById(ticketDTO.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paziente non trovato con ID: " + ticketDTO.getPatientId()));

        Ticket ticket = ticketMapper.toEntity(ticketDTO);
        ticket.setPatient(patient);
        if (ticket.getStatus() == null) {
            ticket.setStatus("OPEN");
        }

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toDto(saved);
    }
}
