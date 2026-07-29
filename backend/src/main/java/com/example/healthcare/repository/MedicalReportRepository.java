package com.example.healthcare.repository;

import com.example.healthcare.entity.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {
    Optional<MedicalReport> findByAppointmentId(Long appointmentId);
    List<MedicalReport> findByAppointmentPatientId(Long patientId);
}
