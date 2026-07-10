package com.example.healthcare.mapper;

import com.example.healthcare.dto.DoctorDTO;
import com.example.healthcare.entity.Doctor;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-10T10:56:03+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class DoctorMapperImpl implements DoctorMapper {

    @Override
    public DoctorDTO toDto(Doctor doctor) {
        if ( doctor == null ) {
            return null;
        }

        DoctorDTO.DoctorDTOBuilder doctorDTO = DoctorDTO.builder();

        doctorDTO.id( doctor.getId() );
        doctorDTO.name( doctor.getName() );
        doctorDTO.specialization( doctor.getSpecialization() );
        doctorDTO.email( doctor.getEmail() );

        return doctorDTO.build();
    }

    @Override
    public Doctor toEntity(DoctorDTO doctorDto) {
        if ( doctorDto == null ) {
            return null;
        }

        Doctor.DoctorBuilder doctor = Doctor.builder();

        doctor.id( doctorDto.getId() );
        doctor.name( doctorDto.getName() );
        doctor.specialization( doctorDto.getSpecialization() );
        doctor.email( doctorDto.getEmail() );

        return doctor.build();
    }
}
