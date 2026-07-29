package com.example.healthcare.mapper;

import com.example.healthcare.dto.DoctorDTO;
import com.example.healthcare.entity.Doctor;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoctorMapperTest {

    private final DoctorMapper mapper = Mappers.getMapper(DoctorMapper.class);

    @Test
    void toDto() {
        Doctor d = new Doctor();
        d.setId(1L);
        d.setName("House");
        d.setSpecialization("Diagnostic");

        DoctorDTO dto = mapper.toDto(d);

        assertEquals(1L, dto.id());
        assertEquals("House", dto.name());
        assertEquals("Diagnostic", dto.specialization());
    }

    @Test
    void toEntity() {
        DoctorDTO dto = DoctorDTO.builder().id(1L).name("House").specialization("Diagnostic").build();

        Doctor d = mapper.toEntity(dto);

        assertEquals(1L, d.getId());
        assertEquals("House", d.getName());
        assertEquals("Diagnostic", d.getSpecialization());
    }
}
