package com.example.healthcare.service;

import com.example.healthcare.dto.TherapyRequest;
import com.example.healthcare.entity.Doctor;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.entity.Therapy;
import com.example.healthcare.repository.DoctorRepository;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.repository.TherapyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TherapyService {

    private final TherapyRepository therapyRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public Therapy createTherapy(TherapyRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Therapy therapy = new Therapy();
        therapy.setPatient(patient);
        therapy.setDoctor(doctor);
        therapy.setDescription(request.description());
        therapy.setStartDate(request.startDate());
        therapy.setEndDate(request.endDate());

        return therapyRepository.save(therapy);
    }

    public List<Therapy> getTherapiesByPatient(Long patientId) {
        return therapyRepository.findByPatientId(patientId);
    }

    public List<Therapy> getTherapiesByDoctor(Long doctorId) {
        return therapyRepository.findByDoctorId(doctorId);
    }
}
