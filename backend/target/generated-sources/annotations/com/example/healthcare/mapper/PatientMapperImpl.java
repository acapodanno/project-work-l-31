package com.example.healthcare.mapper;

import com.example.healthcare.dto.PatientDTO;
import com.example.healthcare.entity.Patient;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-10T10:56:02+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class PatientMapperImpl implements PatientMapper {

    @Override
    public PatientDTO toDto(Patient patient) {
        if ( patient == null ) {
            return null;
        }

        PatientDTO.PatientDTOBuilder patientDTO = PatientDTO.builder();

        patientDTO.id( patient.getId() );
        patientDTO.name( patient.getName() );
        patientDTO.email( patient.getEmail() );
        patientDTO.phone( patient.getPhone() );

        return patientDTO.build();
    }

    @Override
    public Patient toEntity(PatientDTO patientDto) {
        if ( patientDto == null ) {
            return null;
        }

        Patient.PatientBuilder patient = Patient.builder();

        patient.id( patientDto.getId() );
        patient.name( patientDto.getName() );
        patient.email( patientDto.getEmail() );
        patient.phone( patientDto.getPhone() );

        return patient.build();
    }
}
