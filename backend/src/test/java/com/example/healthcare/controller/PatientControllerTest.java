package com.example.healthcare.controller;

import com.example.healthcare.dto.PatientDTO;
import com.example.healthcare.service.PatientService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    private com.example.healthcare.security.JwtUtils jwtUtils;

    @MockBean
    private com.example.healthcare.security.UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllPatients() throws Exception {
        PatientDTO patient = PatientDTO.builder()
                .id(1L)
                .name("Mario Rossi")
                .email("mario@example.com")
                .build();

        when(patientService.getAllPatients()).thenReturn(List.of(patient));

        mockMvc.perform(get("/api/patients")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mario Rossi"))
                .andExpect(jsonPath("$[0].email").value("mario@example.com"));
    }

    @Test
    void testGetPatientById() throws Exception {
        PatientDTO patient = PatientDTO.builder()
                .id(1L)
                .name("Luigi Verdi")
                .email("luigi@example.com")
                .build();

        when(patientService.getPatientById(1L)).thenReturn(patient);

        mockMvc.perform(get("/api/patients/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luigi Verdi"));
    }

    @Test
    void testCreatePatient() throws Exception {
        PatientDTO request = PatientDTO.builder()
                .name("Gino Gini")
                .email("gino@example.com")
                .build();

        PatientDTO response = PatientDTO.builder()
                .id(2L)
                .name("Gino Gini")
                .email("gino@example.com")
                .build();

        when(patientService.createPatient(any(PatientDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Gino Gini"));
    }

    @Test
    void testCreatePatient_ValidationFailed() throws Exception {
        PatientDTO request = PatientDTO.builder()
                .email("invalid-email") // Invalid email, empty name
                .build();

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // validation should fail
    }

    @Test
    void testUpdatePatient() throws Exception {
        PatientDTO request = PatientDTO.builder()
                .name("Gino Gini Modificato")
                .email("gino.mod@example.com")
                .build();

        PatientDTO response = PatientDTO.builder()
                .id(2L)
                .name("Gino Gini Modificato")
                .email("gino.mod@example.com")
                .build();

        when(patientService.updatePatient(eq(2L), any(PatientDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/patients/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gino Gini Modificato"));
    }
}
