package com.example.healthcare.service;

import com.example.healthcare.dto.TicketDTO;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.entity.Ticket;
import com.example.healthcare.mapper.TicketMapper;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.repository.TicketRepository;
import com.example.healthcare.service.strategy.ticket.TicketStatusStrategy;
import com.example.healthcare.service.strategy.ticket.TicketStrategyFactory;
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
    private final TicketStrategyFactory strategyFactory;

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
        Patient patient = patientRepository.findById(ticketDTO.patientId())
                .orElseThrow(() -> new RuntimeException("Paziente non trovato con ID: " + ticketDTO.patientId()));

        Ticket ticket = ticketMapper.toEntity(ticketDTO);
        ticket.setPatient(patient);
        if (ticket.getStatus() == null) {
            ticket.setStatus("OPEN");
        }

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toDto(saved);
    }

    @Transactional
    public TicketDTO updateTicket(Long id, TicketDTO ticketDTO) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket non trovato con ID: " + id));

        if (ticketDTO.title() != null && !ticketDTO.title().isBlank()) {
            ticket.setTitle(ticketDTO.title());
        }
        if (ticketDTO.description() != null && !ticketDTO.description().isBlank()) {
            ticket.setDescription(ticketDTO.description());
        }

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toDto(saved);
    }

    @Transactional
    public TicketDTO updateStatus(Long id, String status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket non trovato con ID: " + id));

        String newStatus = status.toUpperCase();
        ticket.setStatus(newStatus);

        TicketStatusStrategy strategy = strategyFactory.getStrategy(newStatus);
        if (strategy != null) {
            strategy.handleStatusChange(ticket);
        }

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toDto(saved);
    }

    @Transactional
    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new RuntimeException("Ticket non trovato con ID: " + id);
        }
        ticketRepository.deleteById(id);
    }
}
