package com.example.healthcare.controller;

import com.example.healthcare.dto.DashboardStatsDTO;
import com.example.healthcare.security.JwtUtils;
import com.example.healthcare.security.UserDetailsServiceImpl;
import com.example.healthcare.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void getDashboardStats_Success() throws Exception {
        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                .totalPatients(100L)
                .totalDoctors(20L)
                .openTickets(5L)
                .completedAppointments(50L)
                .scheduledAppointments(10L)
                .build();

        when(dashboardService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients").value(100L))
                .andExpect(jsonPath("$.totalDoctors").value(20L))
                .andExpect(jsonPath("$.openTickets").value(5L))
                .andExpect(jsonPath("$.completedAppointments").value(50L))
                .andExpect(jsonPath("$.scheduledAppointments").value(10L));
    }
}
