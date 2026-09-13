package com.example.healthcare.repository;

import com.example.healthcare.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByDoctorIdAndDateOrderByStartTime(Long doctorId, LocalDate date);
    List<Slot> findByDoctorIdOrderByDateAscStartTimeAsc(Long doctorId);
}
