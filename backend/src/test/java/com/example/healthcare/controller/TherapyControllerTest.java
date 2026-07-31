package com.example.healthcare.controller;

import com.example.healthcare.dto.TherapyRequest;
import com.example.healthcare.dto.TherapyResponse;
import com.example.healthcare.security.JwtUtils;
import com.example.healthcare.security.UserDetailsServiceImpl;
import com.example.healthcare.service.TherapyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TherapyController.class)
@AutoConfigureMockMvc(addFilters = false)
class TherapyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TherapyService therapyService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTherapy_Success() throws Exception {
        TherapyRequest req = new TherapyRequest(1L, 2L, "Fisioterapia", LocalDate.now(), LocalDate.now().plusDays(10));
        TherapyResponse response = TherapyResponse.builder().id(1L).description("Fisioterapia").build();

        when(therapyService.createTherapy(any(TherapyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/therapies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Fisioterapia"));
    }

    @Test
    void getTherapiesByPatient_Success() throws Exception {
        TherapyResponse response = TherapyResponse.builder().id(1L).description("Fisioterapia").build();

        when(therapyService.getTherapiesByPatient(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/therapies/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Fisioterapia"));
    }

    @Test
    void getTherapiesByDoctor_Success() throws Exception {
        TherapyResponse response = TherapyResponse.builder().id(1L).description("Fisioterapia").build();

        when(therapyService.getTherapiesByDoctor(2L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/therapies/doctor/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Fisioterapia"));
    }
}
