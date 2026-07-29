package com.example.healthcare.mapper;

import com.example.healthcare.dto.PatientDTO;
import com.example.healthcare.entity.Patient;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PatientMapperTest {

    private final PatientMapper mapper = Mappers.getMapper(PatientMapper.class);

    @Test
    void toDto() {
        Patient p = new Patient();
        p.setId(1L);
        p.setName("John");
        p.setEmail("john@example.com");

        PatientDTO dto = mapper.toDto(p);

        assertEquals(1L, dto.id());
        assertEquals("John", dto.name());
        assertEquals("john@example.com", dto.email());
    }

    @Test
    void toEntity() {
        PatientDTO dto = PatientDTO.builder().id(1L).name("John").email("john@example.com").build();

        Patient p = mapper.toEntity(dto);

        assertEquals(1L, p.getId());
        assertEquals("John", p.getName());
        assertEquals("john@example.com", p.getEmail());
    }
}
