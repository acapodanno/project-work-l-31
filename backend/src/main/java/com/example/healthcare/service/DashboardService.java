package com.example.healthcare.service;

import com.example.healthcare.dto.DashboardStatsDTO;
import com.example.healthcare.entity.AppointmentStatus;
import com.example.healthcare.repository.AppointmentRepository;
import com.example.healthcare.repository.DoctorRepository;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TicketRepository ticketRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        return DashboardStatsDTO.builder()
                .totalPatients(patientRepository.count())
                .totalDoctors(doctorRepository.count())
                .openTickets(ticketRepository.countByStatus("OPEN"))
                .completedAppointments(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED))
                .scheduledAppointments(appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED))
                .build();
    }
}
