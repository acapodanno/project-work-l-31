package com.example.healthcare.service;

import com.example.healthcare.dto.PatientDTO;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.mapper.PatientMapper;
import com.example.healthcare.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;
    private PatientDTO patientDTO;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setName("Mario Rossi");
        patient.setEmail("mario@example.com");
        patient.setPhone("1234567890");

        patientDTO = PatientDTO.builder()
                .id(1L)
                .name("Mario Rossi")
                .email("mario@example.com")
                .phone("1234567890")
                .build();
    }

    @Test
    void testGetAllPatients() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        List<PatientDTO> result = patientService.getAllPatients();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Mario Rossi", result.get(0).name());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void testGetPatientById_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        PatientDTO result = patientService.getPatientById(1L);

        assertNotNull(result);
        assertEquals("Mario Rossi", result.name());
    }

    @Test
    void testGetPatientById_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> patientService.getPatientById(99L));
        assertEquals("Paziente non trovato con ID: 99", exception.getMessage());
    }

    @Test
    void testCreatePatient() {
        when(patientMapper.toEntity(patientDTO)).thenReturn(patient);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        PatientDTO result = patientService.createPatient(patientDTO);

        assertNotNull(result);
        assertEquals("Mario Rossi", result.name());
        verify(patientRepository, times(1)).save(patient);
    }
}
