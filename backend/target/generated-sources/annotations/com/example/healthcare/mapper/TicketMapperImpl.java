package com.example.healthcare.mapper;

import com.example.healthcare.dto.TicketDTO;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.entity.Ticket;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-10T10:56:03+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class TicketMapperImpl implements TicketMapper {

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public TicketDTO toDto(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }

        TicketDTO.TicketDTOBuilder ticketDTO = TicketDTO.builder();

        ticketDTO.patientId( ticketPatientId( ticket ) );
        ticketDTO.id( ticket.getId() );
        ticketDTO.title( ticket.getTitle() );
        ticketDTO.description( ticket.getDescription() );
        ticketDTO.status( ticket.getStatus() );
        ticketDTO.createdAt( ticket.getCreatedAt() );
        ticketDTO.patient( patientMapper.toDto( ticket.getPatient() ) );

        return ticketDTO.build();
    }

    @Override
    public Ticket toEntity(TicketDTO ticketDto) {
        if ( ticketDto == null ) {
            return null;
        }

        Ticket.TicketBuilder ticket = Ticket.builder();

        ticket.patient( ticketDTOToPatient( ticketDto ) );
        ticket.id( ticketDto.getId() );
        ticket.title( ticketDto.getTitle() );
        ticket.description( ticketDto.getDescription() );
        ticket.status( ticketDto.getStatus() );
        ticket.createdAt( ticketDto.getCreatedAt() );

        return ticket.build();
    }

    private Long ticketPatientId(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Patient patient = ticket.getPatient();
        if ( patient == null ) {
            return null;
        }
        Long id = patient.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected Patient ticketDTOToPatient(TicketDTO ticketDTO) {
        if ( ticketDTO == null ) {
            return null;
        }

        Patient.PatientBuilder patient = Patient.builder();

        patient.id( ticketDTO.getPatientId() );

        return patient.build();
    }
}
