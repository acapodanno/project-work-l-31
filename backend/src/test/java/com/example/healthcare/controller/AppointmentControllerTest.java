package com.example.healthcare.controller;

import com.example.healthcare.dto.AppointmentDTO;
import com.example.healthcare.security.JwtUtils;
import com.example.healthcare.security.UserDetailsServiceImpl;
import com.example.healthcare.service.AppointmentService;
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

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllAppointments_Success() throws Exception {
        AppointmentDTO dto = AppointmentDTO.builder().id(1L).reason("Controllo").status("SCHEDULED").build();

        when(appointmentService.getAllAppointments()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("Controllo"))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    void getAppointmentsByPatientId_Success() throws Exception {
        AppointmentDTO dto = AppointmentDTO.builder().id(1L).reason("Controllo").patientId(1L).build();

        when(appointmentService.getAppointmentsByPatientId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/appointments/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("Controllo"));
    }

    @Test
    void createAppointment_Success() throws Exception {
        // Here we just test validation rules (some fields might be null in builder, but we should satisfy @NotNull and @NotBlank)
        // However, we disabled validation or we can just mock a valid request.
        AppointmentDTO requestDto = AppointmentDTO.builder().patientId(1L).doctorId(2L).reason("Controllo").build();
        // Since we are validating in controller, we need appointmentDate. Let's provide a raw string to skip date parsing issues if any.
        String requestJson = "{\"patientId\":1,\"doctorId\":2,\"reason\":\"Controllo\",\"appointmentDate\":\"2026-01-01T10:00:00\"}";

        AppointmentDTO responseDto = AppointmentDTO.builder().id(1L).patientId(1L).doctorId(2L).reason("Controllo").build();

        when(appointmentService.createAppointment(any(AppointmentDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateStatus_Success() throws Exception {
        AppointmentDTO dto = AppointmentDTO.builder().id(1L).status("COMPLETED").build();

        when(appointmentService.updateStatus(eq(1L), eq("COMPLETED"))).thenReturn(dto);

        mockMvc.perform(patch("/api/appointments/1/status")
                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
