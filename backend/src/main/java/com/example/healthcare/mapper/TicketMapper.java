package com.example.healthcare.mapper;

import com.example.healthcare.dto.TicketDTO;
import com.example.healthcare.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PatientMapper.class})
public interface TicketMapper {
    @Mapping(source = "patient.id", target = "patientId")
    TicketDTO toDto(Ticket ticket);

    @Mapping(source = "patientId", target = "patient.id")
    Ticket toEntity(TicketDTO ticketDto);
}
