package com.example.healthcare.mapper;

import com.example.healthcare.dto.AppointmentDTO;
import com.example.healthcare.entity.Appointment;
import com.example.healthcare.entity.Doctor;
import com.example.healthcare.entity.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppointmentMapperTest {

    private AppointmentMapperImpl mapper = new AppointmentMapperImpl();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mapper, "patientMapper", new PatientMapperImpl());
        ReflectionTestUtils.setField(mapper, "doctorMapper", new DoctorMapperImpl());
    }

    @Test
    void toDto() {
        Patient p = new Patient();
        p.setId(1L);
        Doctor d = new Doctor();
        d.setId(2L);
        Appointment a = new Appointment();
        a.setId(10L);
        a.setPatient(p);
        a.setDoctor(d);
        a.setReason("Test");

        AppointmentDTO dto = mapper.toDto(a);

        assertEquals(10L, dto.id());
        assertEquals(1L, dto.patientId());
        assertEquals(2L, dto.doctorId());
        assertEquals("Test", dto.reason());
    }

    @Test
    void toEntity() {
        AppointmentDTO dto = AppointmentDTO.builder().id(10L).patientId(1L).doctorId(2L).reason("Test").build();

        Appointment a = mapper.toEntity(dto);

        assertEquals(10L, a.getId());
        assertEquals(1L, a.getPatient().getId());
        assertEquals(2L, a.getDoctor().getId());
        assertEquals("Test", a.getReason());
    }
}
