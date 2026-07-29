package com.example.healthcare.controller;

import com.example.healthcare.dto.TicketDTO;
import com.example.healthcare.security.JwtUtils;
import com.example.healthcare.security.UserDetailsServiceImpl;
import com.example.healthcare.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllTickets_Success() throws Exception {
        TicketDTO ticket = TicketDTO.builder().id(1L).title("Issue").status("OPEN").build();

        when(ticketService.getAllTickets()).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Issue"));
    }

    @Test
    void getTicketsByPatientId_Success() throws Exception {
        TicketDTO ticket = TicketDTO.builder().id(1L).title("Issue").status("OPEN").build();

        when(ticketService.getTicketsByPatientId(1L)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Issue"));
    }

    @Test
    void createTicket_Success() throws Exception {
        TicketDTO req = TicketDTO.builder().patientId(1L).title("Issue").description("Desc").build();
        TicketDTO res = TicketDTO.builder().id(1L).patientId(1L).title("Issue").status("OPEN").build();

        when(ticketService.createTicket(any(TicketDTO.class))).thenReturn(res);

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateStatus_Success() throws Exception {
        TicketDTO res = TicketDTO.builder().id(1L).title("Issue").status("CLOSED").build();

        when(ticketService.updateStatus(eq(1L), eq("CLOSED"))).thenReturn(res);

        mockMvc.perform(patch("/api/tickets/1/status")
                .param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }
}
