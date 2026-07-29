package com.example.healthcare.service;

import com.example.healthcare.dto.TherapyRequest;
import com.example.healthcare.entity.Doctor;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.entity.Therapy;
import com.example.healthcare.repository.DoctorRepository;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.repository.TherapyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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

    @InjectMocks
    private TherapyService therapyService;

    @Test
    void createTherapy_Success() {
        TherapyRequest req = new TherapyRequest(1L, 2L, "Fisioterapia", LocalDate.now(), LocalDate.now().plusDays(10));
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        Therapy saved = new Therapy();
        
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(therapyRepository.save(any(Therapy.class))).thenReturn(saved);

        Therapy result = therapyService.createTherapy(req);

        assertNotNull(result);
        verify(therapyRepository, times(1)).save(any(Therapy.class));
    }

    @Test
    void createTherapy_PatientNotFound() {
        TherapyRequest req = new TherapyRequest(1L, 2L, "Fisioterapia", LocalDate.now(), LocalDate.now().plusDays(10));
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> therapyService.createTherapy(req));
        assertEquals("Patient not found", exception.getMessage());
    }

    @Test
    void createTherapy_DoctorNotFound() {
        TherapyRequest req = new TherapyRequest(1L, 2L, "Fisioterapia", LocalDate.now(), LocalDate.now().plusDays(10));
        Patient patient = new Patient();
        
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> therapyService.createTherapy(req));
        assertEquals("Doctor not found", exception.getMessage());
    }

    @Test
    void getTherapiesByPatient() {
        Therapy therapy = new Therapy();
        when(therapyRepository.findByPatientId(1L)).thenReturn(List.of(therapy));

        List<Therapy> result = therapyService.getTherapiesByPatient(1L);

        assertEquals(1, result.size());
        verify(therapyRepository).findByPatientId(1L);
    }

    @Test
    void getTherapiesByDoctor() {
        Therapy therapy = new Therapy();
        when(therapyRepository.findByDoctorId(2L)).thenReturn(List.of(therapy));

        List<Therapy> result = therapyService.getTherapiesByDoctor(2L);

        assertEquals(1, result.size());
        verify(therapyRepository).findByDoctorId(2L);
    }
}
