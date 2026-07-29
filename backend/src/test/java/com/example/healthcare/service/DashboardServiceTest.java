package com.example.healthcare.service;

import com.example.healthcare.dto.DashboardStatsDTO;
import com.example.healthcare.entity.AppointmentStatus;
import com.example.healthcare.repository.AppointmentRepository;
import com.example.healthcare.repository.DoctorRepository;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getStats() {
        when(patientRepository.count()).thenReturn(100L);
        when(doctorRepository.count()).thenReturn(20L);
        when(ticketRepository.countByStatus("OPEN")).thenReturn(5L);
        when(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED)).thenReturn(50L);
        when(appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED)).thenReturn(10L);

        DashboardStatsDTO stats = dashboardService.getStats();

        assertEquals(100L, stats.totalPatients());
        assertEquals(20L, stats.totalDoctors());
        assertEquals(5L, stats.openTickets());
        assertEquals(50L, stats.completedAppointments());
        assertEquals(10L, stats.scheduledAppointments());
    }
}
