package com.example.healthcare;

import com.example.healthcare.entity.AppUser;
import com.example.healthcare.entity.Doctor;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.entity.Role;
import com.example.healthcare.repository.DoctorRepository;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Popola pazienti di test
        if (patientRepository.count() == 0) {
            Patient mario = Patient.builder()
                    .name("Mario Rossi")
                    .email("mario.rossi@example.com")
                    .phone("3331234567")
                    .build();
            patientRepository.save(mario);
            userRepository.save(AppUser.builder()
                    .email(mario.getEmail())
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.PATIENT)
                    .build());

            Patient laura = Patient.builder()
                    .name("Laura Bianchi")
                    .email("laura.bianchi@example.com")
                    .phone("3337654321")
                    .build();
            patientRepository.save(laura);
            userRepository.save(AppUser.builder()
                    .email(laura.getEmail())
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.PATIENT)
                    .build());

            System.out.println("Pazienti di test creati con credenziali.");
        }

        // Popola medici
        if (doctorRepository.count() == 0) {
            Doctor neri = Doctor.builder()
                    .name("Dr. Giovanni Neri")
                    .specialization("Cardiologia")
                    .email("giovanni.neri@healthcare.com")
                    .build();
            doctorRepository.save(neri);
            userRepository.save(AppUser.builder()
                    .email(neri.getEmail())
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.DOCTOR)
                    .build());

            Doctor verdi = Doctor.builder()
                    .name("Dr.ssa Anna Verdi")
                    .specialization("Dermatologia")
                    .email("anna.verdi@healthcare.com")
                    .build();
            doctorRepository.save(verdi);
            userRepository.save(AppUser.builder()
                    .email(verdi.getEmail())
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.DOCTOR)
                    .build());

            Doctor bruno = Doctor.builder()
                    .name("Dr. Roberto Bruno")
                    .specialization("Pediatria")
                    .email("roberto.bruno@healthcare.com")
                    .build();
            doctorRepository.save(bruno);
            userRepository.save(AppUser.builder()
                    .email(bruno.getEmail())
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.DOCTOR)
                    .build());

            Doctor russo = Doctor.builder()
                    .name("Dr.ssa Elena Russo")
                    .specialization("Ortopedia")
                    .email("elena.russo@healthcare.com")
                    .build();
            doctorRepository.save(russo);
            userRepository.save(AppUser.builder()
                    .email(russo.getEmail())
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.DOCTOR)
                    .build());

            Doctor corti = Doctor.builder()
                    .name("Dr. Paolo Corti")
                    .specialization("Medicina Generale")
                    .email("paolo.corti@healthcare.com")
                    .build();
            doctorRepository.save(corti);
            userRepository.save(AppUser.builder()
                    .email(corti.getEmail())
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.DOCTOR)
                    .build());

            System.out.println("Medici clinici creati con credenziali.");
        }

        // Popola support user
        if (userRepository.findByEmail("support@healthcare.com").isEmpty()) {
            userRepository.save(AppUser.builder()
                    .email("support@healthcare.com")
                    .password(passwordEncoder.encode("support123"))
                    .role(Role.SUPPORT)
                    .build());
            System.out.println("Utente Supporto creato.");
        }
    }
}
