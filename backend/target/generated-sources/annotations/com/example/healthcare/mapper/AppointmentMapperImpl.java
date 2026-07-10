package com.example.healthcare.mapper;

import com.example.healthcare.dto.AppointmentDTO;
import com.example.healthcare.entity.Appointment;
import com.example.healthcare.entity.AppointmentStatus;
import com.example.healthcare.entity.Doctor;
import com.example.healthcare.entity.Patient;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-10T10:56:02+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class AppointmentMapperImpl implements AppointmentMapper {

    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public AppointmentDTO toDto(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }

        AppointmentDTO.AppointmentDTOBuilder appointmentDTO = AppointmentDTO.builder();

        appointmentDTO.patientId( appointmentPatientId( appointment ) );
        appointmentDTO.doctorId( appointmentDoctorId( appointment ) );
        appointmentDTO.id( appointment.getId() );
        appointmentDTO.appointmentDate( appointment.getAppointmentDate() );
        appointmentDTO.reason( appointment.getReason() );
        appointmentDTO.notes( appointment.getNotes() );
        if ( appointment.getStatus() != null ) {
            appointmentDTO.status( appointment.getStatus().name() );
        }
        appointmentDTO.patient( patientMapper.toDto( appointment.getPatient() ) );
        appointmentDTO.doctor( doctorMapper.toDto( appointment.getDoctor() ) );

        return appointmentDTO.build();
    }

    @Override
    public Appointment toEntity(AppointmentDTO appointmentDto) {
        if ( appointmentDto == null ) {
            return null;
        }

        Appointment.AppointmentBuilder appointment = Appointment.builder();

        appointment.patient( appointmentDTOToPatient( appointmentDto ) );
        appointment.doctor( appointmentDTOToDoctor( appointmentDto ) );
        appointment.id( appointmentDto.getId() );
        appointment.appointmentDate( appointmentDto.getAppointmentDate() );
        appointment.reason( appointmentDto.getReason() );
        appointment.notes( appointmentDto.getNotes() );
        if ( appointmentDto.getStatus() != null ) {
            appointment.status( Enum.valueOf( AppointmentStatus.class, appointmentDto.getStatus() ) );
        }

        return appointment.build();
    }

    private Long appointmentPatientId(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }
        Patient patient = appointment.getPatient();
        if ( patient == null ) {
            return null;
        }
        Long id = patient.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long appointmentDoctorId(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }
        Doctor doctor = appointment.getDoctor();
        if ( doctor == null ) {
            return null;
        }
        Long id = doctor.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected Patient appointmentDTOToPatient(AppointmentDTO appointmentDTO) {
        if ( appointmentDTO == null ) {
            return null;
        }

        Patient.PatientBuilder patient = Patient.builder();

        patient.id( appointmentDTO.getPatientId() );

        return patient.build();
    }

    protected Doctor appointmentDTOToDoctor(AppointmentDTO appointmentDTO) {
        if ( appointmentDTO == null ) {
            return null;
        }

        Doctor.DoctorBuilder doctor = Doctor.builder();

        doctor.id( appointmentDTO.getDoctorId() );

        return doctor.build();
    }
}
