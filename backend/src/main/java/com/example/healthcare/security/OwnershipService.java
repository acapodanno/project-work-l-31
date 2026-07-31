package com.example.healthcare.security;

import com.example.healthcare.repository.AppointmentRepository;
import com.example.healthcare.repository.DoctorRepository;
import com.example.healthcare.repository.MedicalReportRepository;
import com.example.healthcare.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Regole di autorizzazione a grana fine (ownership) usate nelle espressioni
 * SpEL di @PreAuthorize, dove un semplice controllo per ruolo non basta:
 * es. un PATIENT può leggere/modificare solo le proprie risorse.
 */
@Component("ownership")
@RequiredArgsConstructor
public class OwnershipService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalReportRepository medicalReportRepository;

    public boolean isSelfPatient(Long patientId, Authentication authentication) {
        if (authentication == null || patientId == null) {
            return false;
        }
        return patientRepository.findByEmail(authentication.getName())
                .map(patient -> patientId.equals(patient.getId()))
                .orElse(false);
    }

    public boolean isSelfDoctor(Long doctorId, Authentication authentication) {
        if (authentication == null || doctorId == null) {
            return false;
        }
        return doctorRepository.findByEmail(authentication.getName())
                .map(doctor -> doctorId.equals(doctor.getId()))
                .orElse(false);
    }

    /** DOCTOR/SUPPORT gestiscono qualunque appuntamento; il PATIENT può solo cancellare il proprio. */
    public boolean canManageAppointmentStatus(Long appointmentId, String status, Authentication authentication) {
        if (authentication == null || appointmentId == null) {
            return false;
        }
        if (hasAnyRole(authentication, "DOCTOR", "SUPPORT")) {
            return true;
        }
        if (status == null || !"CANCELLED".equalsIgnoreCase(status)) {
            return false;
        }
        return appointmentRepository.findById(appointmentId)
                .map(appt -> appt.getPatient().getEmail().equals(authentication.getName()))
                .orElse(false);
    }

    /** DOCTOR/SUPPORT possono modificare qualunque appuntamento; il PATIENT solo i propri. */
    public boolean canEditAppointment(Long appointmentId, Authentication authentication) {
        if (authentication == null || appointmentId == null) {
            return false;
        }
        if (hasAnyRole(authentication, "DOCTOR", "SUPPORT")) {
            return true;
        }
        return appointmentRepository.findById(appointmentId)
                .map(appt -> appt.getPatient().getEmail().equals(authentication.getName()))
                .orElse(false);
    }

    /** Un referto è leggibile dal paziente/medico dell'appuntamento associato, o da SUPPORT per assistenza. */
    public boolean canAccessReport(Long appointmentId, Authentication authentication) {
        if (authentication == null || appointmentId == null) {
            return false;
        }
        if (hasAnyRole(authentication, "SUPPORT")) {
            return true;
        }
        return appointmentRepository.findById(appointmentId)
                .map(appt -> appt.getPatient().getEmail().equals(authentication.getName())
                        || appt.getDoctor().getEmail().equals(authentication.getName()))
                .orElse(false);
    }

    /** Il download di un referto è consentito solo a chi può accedere all'appuntamento a cui è collegato. */
    public boolean canDownloadReport(String fileName, Authentication authentication) {
        if (authentication == null || fileName == null) {
            return false;
        }
        if (hasAnyRole(authentication, "SUPPORT")) {
            return true;
        }
        return medicalReportRepository.findByFileName(fileName)
                .map(report -> report.getAppointment().getPatient().getEmail().equals(authentication.getName())
                        || report.getAppointment().getDoctor().getEmail().equals(authentication.getName()))
                .orElse(false);
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        for (String role : roles) {
            String target = "ROLE_" + role;
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals(target)) {
                    return true;
                }
            }
        }
        return false;
    }
}
