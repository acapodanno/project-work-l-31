package com.example.healthcare.service;

import com.example.healthcare.dto.TicketDTO;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.entity.Ticket;
import com.example.healthcare.mapper.TicketMapper;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.repository.TicketRepository;
import com.example.healthcare.service.strategy.ticket.TicketStatusStrategy;
import com.example.healthcare.service.strategy.ticket.TicketStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock(lenient = true)
    private TicketStatusStrategy mockStrategy;

    private TicketStrategyFactory strategyFactory;
    private TicketService ticketService;

    private Ticket ticket;
    private TicketDTO ticketDTO;
    private Patient patient;

    @BeforeEach
    void setUp() {
        when(mockStrategy.getSupportedStatus()).thenReturn("CLOSED");
        strategyFactory = new TicketStrategyFactory(List.of(mockStrategy));
        
        ticketService = new TicketService(ticketRepository, patientRepository, ticketMapper, strategyFactory);

        patient = new Patient();
        patient.setId(1L);

        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus("OPEN");
        ticket.setPatient(patient);

        ticketDTO = TicketDTO.builder()
                .id(1L)
                .status("OPEN")
                .patientId(1L)
                .build();
    }

    @Test
    void testCreateTicket() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(ticketMapper.toEntity(ticketDTO)).thenReturn(ticket);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toDto(ticket)).thenReturn(ticketDTO);

        TicketDTO result = ticketService.createTicket(ticketDTO);

        assertNotNull(result);
        assertEquals("OPEN", result.status());
        verify(patientRepository, times(1)).findById(1L);
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    void testUpdateStatus_WithStrategy() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toDto(ticket)).thenReturn(ticketDTO);

        ticketService.updateStatus(1L, "CLOSED");

        assertEquals("CLOSED", ticket.getStatus());
        verify(mockStrategy, times(1)).handleStatusChange(ticket);
        verify(ticketRepository, times(1)).save(ticket);
    }
}
