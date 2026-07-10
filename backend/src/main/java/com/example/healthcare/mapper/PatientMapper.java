package com.example.healthcare.mapper;

import com.example.healthcare.dto.PatientDTO;
import com.example.healthcare.entity.Patient;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientDTO toDto(Patient patient);
    Patient toEntity(PatientDTO patientDto);
}
