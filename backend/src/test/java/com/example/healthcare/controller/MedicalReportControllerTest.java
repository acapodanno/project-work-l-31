package com.example.healthcare.controller;

import com.example.healthcare.dto.MedicalReportResponse;
import com.example.healthcare.security.JwtUtils;
import com.example.healthcare.security.UserDetailsServiceImpl;
import com.example.healthcare.service.FileStorageService;
import com.example.healthcare.service.MedicalReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicalReportController.class)
@AutoConfigureMockMvc(addFilters = false) // Ignore security filters for unit test
class MedicalReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicalReportService medicalReportService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadReport_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", "dummy data".getBytes());
        MedicalReportResponse res = MedicalReportResponse.builder().id(1L).fileName("report.pdf").build();

        when(medicalReportService.uploadReport(eq(1L), any(), eq("patient@example.com"))).thenReturn(res);

        mockMvc.perform(multipart("/api/reports/upload")
                .file(file)
                .param("appointmentId", "1")
                // WithMockUser provides Principal, but MockMvc multipart can be tricky without security context
                .principal(() -> "patient@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("report.pdf"));
    }

    @Test
    void addDoctorNotes_Success() throws Exception {
        MedicalReportResponse res = MedicalReportResponse.builder().id(1L).doctorNotes("Note aggiunte").build();

        when(medicalReportService.addDoctorNotes(eq(1L), eq("Note aggiunte"), eq("doctor@example.com"))).thenReturn(res);

        String json = "{\"notes\":\"Note aggiunte\"}";

        mockMvc.perform(put("/api/reports/1/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .principal(() -> "doctor@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorNotes").value("Note aggiunte"));
    }

    @Test
    void getReportByAppointmentId_Success() throws Exception {
        MedicalReportResponse res = MedicalReportResponse.builder().id(1L).fileName("report.pdf").build();

        when(medicalReportService.getReportByAppointmentId(1L)).thenReturn(res);

        mockMvc.perform(get("/api/reports/appointment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("report.pdf"));
    }

    @Test
    void getReportsByPatientId_Success() throws Exception {
        MedicalReportResponse res = MedicalReportResponse.builder().id(1L).fileName("report.pdf").build();

        when(medicalReportService.getReportsByPatientId(1L)).thenReturn(List.of(res));

        mockMvc.perform(get("/api/reports/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("report.pdf"));
    }
}
