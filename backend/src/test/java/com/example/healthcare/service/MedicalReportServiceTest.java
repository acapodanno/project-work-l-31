package com.example.healthcare.service;

import com.example.healthcare.dto.MedicalReportResponse;
import com.example.healthcare.entity.Appointment;
import com.example.healthcare.entity.AppointmentStatus;
import com.example.healthcare.entity.Doctor;
import com.example.healthcare.entity.MedicalReport;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.repository.AppointmentRepository;
import com.example.healthcare.repository.MedicalReportRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalReportServiceTest {

    @Mock
    private MedicalReportRepository medicalReportRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private DataExtractionAgent dataExtractionAgent;

    @InjectMocks
    private MedicalReportService medicalReportService;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void uploadReport_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        Appointment appt = new Appointment();
        appt.setId(1L);
        Patient patient = new Patient();
        patient.setEmail("patient@example.com");
        patient.setName("John");
        Doctor doctor = new Doctor();
        doctor.setName("Doc");
        appt.setPatient(patient);
        appt.setDoctor(doctor);

        MedicalReport saved = MedicalReport.builder()
                .id(1L)
                .appointment(appt)
                .fileName("stored_test.pdf")
                .extractedData("data")
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appt));
        when(medicalReportRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
        when(fileStorageService.storeFile(file)).thenReturn("stored_test.pdf");
        when(dataExtractionAgent.extractData("test.pdf")).thenReturn("data");
        when(medicalReportRepository.save(any(MedicalReport.class))).thenReturn(saved);

        MedicalReportResponse res = medicalReportService.uploadReport(1L, file, "patient@example.com");

        assertNotNull(res);
        assertEquals("stored_test.pdf", res.fileName());
        assertEquals("data", res.extractedData());
    }

    @Test
    void uploadReport_WrongPatient() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        Appointment appt = new Appointment();
        Patient patient = new Patient();
        patient.setEmail("other@example.com");
        appt.setPatient(patient);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appt));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> medicalReportService.uploadReport(1L, file, "patient@example.com"));
        assertEquals("Non autorizzato a caricare referti per questo appuntamento", ex.getMessage());
    }

    @Test
    void uploadReport_AlreadyExists() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        Appointment appt = new Appointment();
        appt.setId(1L);
        Patient patient = new Patient();
        patient.setEmail("patient@example.com");
        appt.setPatient(patient);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appt));
        when(medicalReportRepository.findByAppointmentId(1L)).thenReturn(Optional.of(new MedicalReport()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> medicalReportService.uploadReport(1L, file, "patient@example.com"));
        assertEquals("Esiste già un documento per questo appuntamento", ex.getMessage());
    }

    @Test
    void addDoctorNotes_Success() {
        Appointment appt = new Appointment();
        Doctor doctor = new Doctor();
        doctor.setEmail("doc@example.com");
        doctor.setName("Doc");
        Patient patient = new Patient();
        patient.setName("John");
        appt.setDoctor(doctor);
        appt.setPatient(patient);
        appt.setId(1L);

        MedicalReport report = MedicalReport.builder().appointment(appt).fileName("f.pdf").build();

        when(medicalReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(medicalReportRepository.save(report)).thenReturn(report);

        MedicalReportResponse res = medicalReportService.addDoctorNotes(1L, "notes", "doc@example.com");

        assertEquals("notes", report.getDoctorNotes());
        assertEquals(AppointmentStatus.COMPLETED, appt.getStatus());
        verify(appointmentRepository).save(appt);
    }

    @Test
    void addDoctorNotes_WrongDoctor() {
        Appointment appt = new Appointment();
        Doctor doctor = new Doctor();
        doctor.setEmail("other@example.com");
        appt.setDoctor(doctor);

        MedicalReport report = MedicalReport.builder().appointment(appt).build();

        when(medicalReportRepository.findById(1L)).thenReturn(Optional.of(report));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> medicalReportService.addDoctorNotes(1L, "notes", "doc@example.com"));
        assertEquals("Non autorizzato ad aggiungere note a questo referto", ex.getMessage());
    }

    @Test
    void getReportByAppointmentId() {
        Appointment appt = new Appointment();
        appt.setId(1L);
        Patient p = new Patient(); p.setName("P");
        Doctor d = new Doctor(); d.setName("D");
        appt.setPatient(p);
        appt.setDoctor(d);

        MedicalReport report = MedicalReport.builder().appointment(appt).fileName("f.pdf").build();

        when(medicalReportRepository.findByAppointmentId(1L)).thenReturn(Optional.of(report));

        MedicalReportResponse res = medicalReportService.getReportByAppointmentId(1L);
        assertNotNull(res);
    }

    @Test
    void getReportsByPatientId() {
        Appointment appt = new Appointment();
        appt.setId(1L);
        Patient p = new Patient(); p.setName("P");
        Doctor d = new Doctor(); d.setName("D");
        appt.setPatient(p);
        appt.setDoctor(d);

        MedicalReport report = MedicalReport.builder().appointment(appt).fileName("f.pdf").build();

        when(medicalReportRepository.findByAppointmentPatientId(2L)).thenReturn(List.of(report));

        List<MedicalReportResponse> res = medicalReportService.getReportsByPatientId(2L);
        assertEquals(1, res.size());
    }
}
