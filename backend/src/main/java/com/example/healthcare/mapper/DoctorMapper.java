package com.example.healthcare.mapper;

import com.example.healthcare.dto.DoctorDTO;
import com.example.healthcare.entity.Doctor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DoctorMapper {
    DoctorDTO toDto(Doctor doctor);
    Doctor toEntity(DoctorDTO doctorDto);
}
