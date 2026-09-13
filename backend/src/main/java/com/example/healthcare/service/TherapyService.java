package com.example.healthcare.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TherapyService {

    private final TherapyRepository therapyRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;

    @Transactional
    public TherapyResponse createTherapy(TherapyRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new RuntimeException("La data di fine terapia non può precedere la data di inizio");
        }

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new RuntimeException("Paziente non trovato con ID: " + request.patientId()));

        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new RuntimeException("Medico non trovato con ID: " + request.doctorId()));

        Therapy therapy = new Therapy();
        therapy.setPatient(patient);
        therapy.setDoctor(doctor);
        therapy.setDescription(request.description());
        therapy.setStartDate(request.startDate());
        therapy.setEndDate(request.endDate());

        if (request.appointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.appointmentId())
                    .orElseThrow(() -> new RuntimeException("Appuntamento non trovato con ID: " + request.appointmentId()));

            if (!appointment.getPatient().getId().equals(request.patientId()) || !appointment.getDoctor().getId().equals(request.doctorId())) {
                throw new RuntimeException("L'appuntamento indicato non appartiene a questo paziente e medico");
            }

            therapy.setAppointment(appointment);
        }

        return toResponse(therapyRepository.save(therapy));
    }

    @Transactional(readOnly = true)
    public List<TherapyResponse> getTherapiesByPatient(Long patientId) {
        return therapyRepository.findByPatientId(patientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TherapyResponse> getTherapiesByDoctor(Long doctorId) {
        return therapyRepository.findByDoctorId(doctorId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TherapyResponse toResponse(Therapy therapy) {
        Appointment appointment = therapy.getAppointment();
        return TherapyResponse.builder()
                .id(therapy.getId())
                .patient(patientMapper.toDto(therapy.getPatient()))
                .doctor(doctorMapper.toDto(therapy.getDoctor()))
                .description(therapy.getDescription())
                .startDate(therapy.getStartDate())
                .endDate(therapy.getEndDate())
                .createdAt(therapy.getCreatedAt())
                .appointmentId(appointment != null ? appointment.getId() : null)
                .appointmentDate(appointment != null ? appointment.getAppointmentDate() : null)
                .appointmentReason(appointment != null ? appointment.getReason() : null)
                .build();
    }
}
