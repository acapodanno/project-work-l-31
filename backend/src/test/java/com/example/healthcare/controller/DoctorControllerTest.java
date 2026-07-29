package com.example.healthcare.controller;

import com.example.healthcare.dto.DoctorDTO;
import com.example.healthcare.security.JwtUtils;
import com.example.healthcare.security.UserDetailsServiceImpl;
import com.example.healthcare.service.DoctorService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorController.class)
@AutoConfigureMockMvc(addFilters = false)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllDoctors_Success() throws Exception {
        DoctorDTO doc1 = DoctorDTO.builder().id(1L).name("Doc1").specialization("Spec1").email("doc1@example.com").build();

        when(doctorService.getAllDoctors()).thenReturn(List.of(doc1));

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Doc1"))
                .andExpect(jsonPath("$[0].specialization").value("Spec1"));
    }

    @Test
    void getDoctorById_Success() throws Exception {
        DoctorDTO doc = DoctorDTO.builder().id(1L).name("Doc1").specialization("Spec1").email("doc1@example.com").build();

        when(doctorService.getDoctorById(1L)).thenReturn(doc);

        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Doc1"));
    }

    @Test
    void createDoctor_Success() throws Exception {
        DoctorDTO requestDto = DoctorDTO.builder().name("Doc1").specialization("Spec1").email("doc1@example.com").build();
        DoctorDTO responseDto = DoctorDTO.builder().id(1L).name("Doc1").specialization("Spec1").email("doc1@example.com").build();

        when(doctorService.createDoctor(any(DoctorDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Doc1"));
    }
}
