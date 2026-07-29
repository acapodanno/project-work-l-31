package com.example.healthcare.mapper;

import com.example.healthcare.dto.TicketDTO;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.entity.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TicketMapperTest {

    private TicketMapperImpl mapper = new TicketMapperImpl();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mapper, "patientMapper", new PatientMapperImpl());
    }

    @Test
    void toDto() {
        Patient p = new Patient();
        p.setId(1L);
        Ticket t = new Ticket();
        t.setId(10L);
        t.setPatient(p);
        t.setTitle("Issue");

        TicketDTO dto = mapper.toDto(t);

        assertEquals(10L, dto.id());
        assertEquals(1L, dto.patientId());
        assertEquals("Issue", dto.title());
    }

    @Test
    void toEntity() {
        TicketDTO dto = TicketDTO.builder().id(10L).patientId(1L).title("Issue").build();

        Ticket t = mapper.toEntity(dto);

        assertEquals(10L, t.getId());
        assertEquals(1L, t.getPatient().getId());
        assertEquals("Issue", t.getTitle());
    }
}
