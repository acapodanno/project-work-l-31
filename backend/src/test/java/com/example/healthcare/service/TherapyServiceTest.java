package com.example.healthcare.service;

import com.example.healthcare.dto.DoctorDTO;
import com.example.healthcare.dto.PatientDTO;
import com.example.healthcare.dto.TherapyRequest;
import com.example.healthcare.dto.TherapyResponse;
import com.example.healthcare.entity.Appointment;
import com.example.healthcare.entity.Doctor;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.entity.Therapy;
import com.example.healthcare.mapper.DoctorMapper;
import com.example.healthcare.mapper.PatientMapper;
import com.example.healthcare.repository.AppointmentRepository;
import com.example.healthcare.repository.DoctorRepository;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.repository.TherapyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TherapyServiceTest {

    @Mock
    private TherapyRepository therapyRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private TherapyService therapyService;

    @Test
    void createTherapy_Success() {
        TherapyRequest req = new TherapyRequest(1L, 2L, "Fisioterapia", LocalDate.now(), LocalDate.now().plusDays(10), null);
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        Therapy saved = new Therapy();
        saved.setPatient(patient);
        saved.setDoctor(doctor);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(therapyRepository.save(any(Therapy.class))).thenReturn(saved);
        when(patientMapper.toDto(patient)).thenReturn(PatientDTO.builder().id(1L).build());
        when(doctorMapper.toDto(doctor)).thenReturn(DoctorDTO.builder().id(2L).build());

        TherapyResponse result = therapyService.createTherapy(req);

        assertNotNull(result);
        verify(therapyRepository, times(1)).save(any(Therapy.class));
    }

    @Test
    void createTherapy_EndDateBeforeStartDate() {
        TherapyRequest req = new TherapyRequest(1L, 2L, "Fisioterapia", LocalDate.now(), LocalDate.now().minusDays(1), null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> therapyService.createTherapy(req));
        assertEquals("La data di fine terapia non può precedere la data di inizio", exception.getMessage());
        verifyNoInteractions(therapyRepository);
    }

    @Test
    void createTherapy_PatientNotFound() {
        TherapyRequest req = new TherapyRequest(1L, 2L, "Fisioterapia", LocalDate.now(), LocalDate.now().plusDays(10), null);
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> therapyService.createTherapy(req));
        assertEquals("Paziente non trovato con ID: 1", exception.getMessage());
    }

    @Test
    void createTherapy_DoctorNotFound() {
        TherapyRequest req = new TherapyRequest(1L, 2L, "Fisioterapia", LocalDate.now(), LocalDate.now().plusDays(10), null);
        Patient patient = new Patient();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> therapyService.createTherapy(req));
        assertEquals("Medico non trovato con ID: 2", exception.getMessage());
    }

    @Test
    void createTherapy_WithLinkedAppointment_Success() {
        TherapyRequest req = new TherapyRequest(1L, 2L, "Fisioterapia", LocalDate.now(), LocalDate.now().plusDays(10), 5L);
        Patient patient = new Patient();
        patient.setId(1L);
        Doctor doctor = new Doctor();
        doctor.setId(2L);

        Appointment appointment = new Appointment();
        appointment.setId(5L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setReason("Visita di controllo");
        appointment.setAppointmentDate(LocalDateTime.of(2026, 8, 5, 10, 0));

        Therapy saved = new Therapy();
        saved.setPatient(patient);
        saved.setDoctor(doctor);
        saved.setAppointment(appointment);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));
        when(therapyRepository.save(any(Therapy.class))).thenReturn(saved);
        when(patientMapper.toDto(patient)).thenReturn(PatientDTO.builder().id(1L).build());
        when(doctorMapper.toDto(doctor)).thenReturn(DoctorDTO.builder().id(2L).build());

        TherapyResponse result = therapyService.createTherapy(req);

        assertEquals(5L, result.appointmentId());
        assertEquals("Visita di controllo", result.appointmentReason());
        assertNotNull(result.appointmentDate());
    }

    @Test
    void createTherapy_AppointmentNotFound() {
        TherapyRequest req = new TherapyRequest(1L, 2L, "Fisioterapia", LocalDate.now(), LocalDate.now().plusDays(10), 5L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(new Doctor()));
        when(appointmentRepository.findById(5L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> therapyService.createTherapy(req));
        assertEquals("Appuntamento non trovato con ID: 5", exception.getMessage());
        verify(therapyRepository, never()).save(any());
    }

    @Test
    void createTherapy_AppointmentBelongsToDifferentPatient() {
        TherapyRequest req = new TherapyRequest(1L, 2L, "Fisioterapia", LocalDate.now(), LocalDate.now().plusDays(10), 5L);
        Patient requestPatient = new Patient();
        requestPatient.setId(1L);
        Doctor doctor = new Doctor();
        doctor.setId(2L);

        Patient otherPatient = new Patient();
        otherPatient.setId(99L);

        Appointment appointment = new Appointment();
        appointment.setId(5L);
        appointment.setPatient(otherPatient);
        appointment.setDoctor(doctor);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(requestPatient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> therapyService.createTherapy(req));
        assertEquals("L'appuntamento indicato non appartiene a questo paziente e medico", exception.getMessage());
        verify(therapyRepository, never()).save(any());
    }

    @Test
    void getTherapiesByPatient() {
        Therapy therapy = new Therapy();
        therapy.setPatient(new Patient());
        therapy.setDoctor(new Doctor());
        when(therapyRepository.findByPatientId(1L)).thenReturn(List.of(therapy));

        List<TherapyResponse> result = therapyService.getTherapiesByPatient(1L);

        assertEquals(1, result.size());
        verify(therapyRepository).findByPatientId(1L);
    }

    @Test
    void getTherapiesByDoctor() {
        Therapy therapy = new Therapy();
        therapy.setPatient(new Patient());
        therapy.setDoctor(new Doctor());
        when(therapyRepository.findByDoctorId(2L)).thenReturn(List.of(therapy));

        List<TherapyResponse> result = therapyService.getTherapiesByDoctor(2L);

        assertEquals(1, result.size());
        verify(therapyRepository).findByDoctorId(2L);
    }
}
