package com.example.healthcare.repository;

import com.example.healthcare.entity.Therapy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TherapyRepository extends JpaRepository<Therapy, Long> {
    List<Therapy> findByPatientId(Long patientId);
    List<Therapy> findByDoctorId(Long doctorId);
}
