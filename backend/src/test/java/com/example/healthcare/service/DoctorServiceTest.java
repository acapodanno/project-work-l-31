package com.example.healthcare.service;

import com.example.healthcare.dto.DoctorDTO;
import com.example.healthcare.entity.Doctor;
import com.example.healthcare.mapper.DoctorMapper;
import com.example.healthcare.repository.DoctorRepository;
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
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void getAllDoctors() {
        Doctor doc = new Doctor();
        DoctorDTO dto = DoctorDTO.builder().build();
        
        when(doctorRepository.findAll()).thenReturn(List.of(doc));
        when(doctorMapper.toDto(doc)).thenReturn(dto);

        List<DoctorDTO> result = doctorService.getAllDoctors();

        assertEquals(1, result.size());
        verify(doctorRepository, times(1)).findAll();
        verify(doctorMapper, times(1)).toDto(doc);
    }

    @Test
    void getDoctorById_Success() {
        Doctor doc = new Doctor();
        DoctorDTO dto = DoctorDTO.builder().build();

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(doctorMapper.toDto(doc)).thenReturn(dto);

        DoctorDTO result = doctorService.getDoctorById(1L);

        assertNotNull(result);
        verify(doctorRepository, times(1)).findById(1L);
    }

    @Test
    void getDoctorById_NotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> doctorService.getDoctorById(1L));
        assertEquals("Medico non trovato con ID: 1", exception.getMessage());
    }

    @Test
    void createDoctor() {
        Doctor doc = new Doctor();
        Doctor saved = new Doctor();
        DoctorDTO dto = DoctorDTO.builder().build();

        when(doctorMapper.toEntity(dto)).thenReturn(doc);
        when(doctorRepository.save(doc)).thenReturn(saved);
        when(doctorMapper.toDto(saved)).thenReturn(dto);

        DoctorDTO result = doctorService.createDoctor(dto);

        assertNotNull(result);
        verify(doctorRepository).save(doc);
    }

    @Test
    void updateDoctor_Success() {
        Doctor doc = new Doctor();
        Doctor saved = new Doctor();
        DoctorDTO update = DoctorDTO.builder()
                .specialization("Cardiologia interventistica")
                .bio("Nuova bio")
                .experienceYears(16)
                .workingHours("Lun-Ven 09-17")
                .build();
        DoctorDTO responseDto = DoctorDTO.builder().id(1L).build();

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(doctorRepository.save(doc)).thenReturn(saved);
        when(doctorMapper.toDto(saved)).thenReturn(responseDto);

        DoctorDTO result = doctorService.updateDoctor(1L, update);

        assertNotNull(result);
        assertEquals("Cardiologia interventistica", doc.getSpecialization());
        assertEquals("Nuova bio", doc.getBio());
        assertEquals(16, doc.getExperienceYears());
        assertEquals("Lun-Ven 09-17", doc.getWorkingHours());
        verify(doctorRepository).save(doc);
    }

    @Test
    void updateDoctor_NotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> doctorService.updateDoctor(1L, DoctorDTO.builder().build()));
        assertEquals("Medico non trovato con ID: 1", exception.getMessage());
    }
}
