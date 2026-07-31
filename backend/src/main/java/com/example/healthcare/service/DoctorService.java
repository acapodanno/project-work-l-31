package com.example.healthcare.service;

import com.example.healthcare.dto.DoctorDTO;
import com.example.healthcare.entity.Doctor;
import com.example.healthcare.mapper.DoctorMapper;
import com.example.healthcare.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    @Transactional(readOnly = true)
    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(doctorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medico non trovato con ID: " + id));
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public DoctorDTO createDoctor(DoctorDTO doctorDTO) {
        Doctor doctor = doctorMapper.toEntity(doctorDTO);
        Doctor saved = doctorRepository.save(doctor);
        return doctorMapper.toDto(saved);
    }

    @Transactional
    public DoctorDTO updateDoctor(Long id, DoctorDTO doctorDTO) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medico non trovato con ID: " + id));

        if (doctorDTO.specialization() != null && !doctorDTO.specialization().isBlank()) {
            doctor.setSpecialization(doctorDTO.specialization());
        }
        if (doctorDTO.bio() != null) {
            doctor.setBio(doctorDTO.bio());
        }
        if (doctorDTO.experienceYears() != null) {
            doctor.setExperienceYears(doctorDTO.experienceYears());
        }
        if (doctorDTO.workingHours() != null) {
            doctor.setWorkingHours(doctorDTO.workingHours());
        }

        Doctor saved = doctorRepository.save(doctor);
        return doctorMapper.toDto(saved);
    }
}
