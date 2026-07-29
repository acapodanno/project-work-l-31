package com.example.healthcare.service;

import com.example.healthcare.dto.AppointmentDTO;
import com.example.healthcare.entity.Appointment;
import com.example.healthcare.entity.AppointmentStatus;
import com.example.healthcare.entity.Doctor;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.mapper.AppointmentMapper;
import com.example.healthcare.repository.AppointmentRepository;
import com.example.healthcare.repository.DoctorRepository;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.service.strategy.appointment.AppointmentStatusStrategy;
import com.example.healthcare.service.strategy.appointment.AppointmentStrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private AppointmentStrategyFactory strategyFactory;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void getAllAppointments() {
        Appointment appt = new Appointment();
        AppointmentDTO dto = AppointmentDTO.builder()
                .id(1L)
                .patientId(2L)
                .doctorId(3L)
                .reason("Visita")
                .status("SCHEDULED")
                .build();

        when(appointmentRepository.findAll()).thenReturn(List.of(appt));
        when(appointmentMapper.toDto(appt)).thenReturn(dto);

        List<AppointmentDTO> result = appointmentService.getAllAppointments();

        assertEquals(1, result.size());
        verify(appointmentRepository).findAll();
    }

    @Test
    void getAppointmentsByPatientId() {
        Appointment appt = new Appointment();
        AppointmentDTO dto = AppointmentDTO.builder()
                .id(1L)
                .patientId(2L)
                .doctorId(3L)
                .reason("Visita")
                .status("SCHEDULED")
                .build();

        when(appointmentRepository.findByPatientId(2L)).thenReturn(List.of(appt));
        when(appointmentMapper.toDto(appt)).thenReturn(dto);

        List<AppointmentDTO> result = appointmentService.getAppointmentsByPatientId(2L);

        assertEquals(1, result.size());
        verify(appointmentRepository).findByPatientId(2L);
    }

    @Test
    void createAppointment_Success() {
        AppointmentDTO dto = AppointmentDTO.builder()
                .id(1L)
                .patientId(2L)
                .doctorId(3L)
                .reason("Visita")
                .status("SCHEDULED")
                .build();
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        Appointment appt = new Appointment();
        Appointment saved = new Appointment();

        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(3L)).thenReturn(Optional.of(doctor));
        when(appointmentMapper.toEntity(dto)).thenReturn(appt);
        when(appointmentRepository.save(appt)).thenReturn(saved);
        when(appointmentMapper.toDto(saved)).thenReturn(dto);

        AppointmentDTO result = appointmentService.createAppointment(dto);

        assertNotNull(result);
        assertEquals(AppointmentStatus.SCHEDULED, appt.getStatus());
        verify(appointmentRepository).save(appt);
    }

    @Test
    void createAppointment_PatientNotFound() {
        AppointmentDTO dto = AppointmentDTO.builder()
                .id(1L)
                .patientId(2L)
                .doctorId(3L)
                .reason("Visita")
                .status("SCHEDULED")
                .build();

        when(patientRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> appointmentService.createAppointment(dto));
        assertEquals("Paziente non trovato con ID: 2", exception.getMessage());
    }

    @Test
    void updateStatus_Success_WithStrategy() {
        Appointment appt = new Appointment();
        appt.setId(1L);
        Appointment saved = new Appointment();
        AppointmentDTO dto = AppointmentDTO.builder()
                .id(1L)
                .patientId(2L)
                .doctorId(3L)
                .reason("Visita")
                .status("COMPLETED")
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appt));
        AppointmentStatusStrategy strategy = mock(AppointmentStatusStrategy.class);
        when(strategyFactory.getStrategy(AppointmentStatus.COMPLETED)).thenReturn(strategy);
        when(appointmentRepository.save(appt)).thenReturn(saved);
        when(appointmentMapper.toDto(saved)).thenReturn(dto);

        AppointmentDTO result = appointmentService.updateStatus(1L, "COMPLETED");

        assertEquals(AppointmentStatus.COMPLETED, appt.getStatus());
        verify(strategy).handleStatusChange(appt);
        verify(appointmentRepository).save(appt);
        assertNotNull(result);
    }

    @Test
    void updateStatus_Success_NoStrategy() {
        Appointment appt = new Appointment();
        appt.setId(1L);
        Appointment saved = new Appointment();
        AppointmentDTO dto = AppointmentDTO.builder()
                .id(1L)
                .patientId(2L)
                .doctorId(3L)
                .reason("Visita")
                .status("SCHEDULED")
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appt));
        when(strategyFactory.getStrategy(AppointmentStatus.SCHEDULED)).thenReturn(null);
        when(appointmentRepository.save(appt)).thenReturn(saved);
        when(appointmentMapper.toDto(saved)).thenReturn(dto);

        AppointmentDTO result = appointmentService.updateStatus(1L, "SCHEDULED");

        assertEquals(AppointmentStatus.SCHEDULED, appt.getStatus());
        verify(appointmentRepository).save(appt);
    }

    @Test
    void updateStatus_NotFound() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> appointmentService.updateStatus(1L, "COMPLETED"));
        assertEquals("Appuntamento non trovato con ID: 1", exception.getMessage());
    }
}
