package com.example.healthcare;

import com.example.healthcare.entity.AppUser;
import com.example.healthcare.entity.Doctor;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.entity.Role;
import com.example.healthcare.repository.DoctorRepository;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
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

            log.info("Pazienti di test creati con credenziali.");
        }

        // Popola medici
        if (doctorRepository.count() == 0) {
            Doctor neri = Doctor.builder()
                    .name("Dr. Giovanni Neri")
                    .specialization("Cardiologia")
                    .email("giovanni.neri@healthcare.com")
                    .experienceYears(15)
                    .bio("Specialista in cardiologia clinica e interventistica. Il Dr. Neri ha una vasta esperienza nella diagnosi e nel trattamento delle patologie cardiovascolari, ponendo sempre il paziente al centro del percorso di cura.")
                    .workingHours("Lunedì - Venerdì: 09:00 - 17:00")
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
                    .experienceYears(8)
                    .bio("Dermatologa appassionata e attenta ai dettagli. Si occupa di dermatologia oncologica e cura degli inestetismi della pelle. Crede fortemente nella prevenzione e nell'educazione del paziente.")
                    .workingHours("Martedì - Giovedì: 10:00 - 18:00")
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
                    .experienceYears(20)
                    .bio("Pediatra con oltre 20 anni di esperienza. Il suo approccio empatico e rassicurante aiuta sia i bambini che i genitori ad affrontare con serenità ogni visita medica.")
                    .workingHours("Lunedì - Mercoledì - Venerdì: 08:30 - 14:30")
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
                    .experienceYears(12)
                    .bio("Esperta in traumatologia sportiva e chirurgia protesica. La Dott.ssa Russo lavora per far tornare i suoi pazienti a muoversi liberamente e senza dolore nel minor tempo possibile.")
                    .workingHours("Lunedì - Venerdì: 09:00 - 13:00, 15:00 - 19:00")
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
                    .experienceYears(25)
                    .bio("Medico di medicina generale, è il punto di riferimento per le famiglie. Ascolto, pazienza e dedizione sono i cardini della sua pratica medica quotidiana.")
                    .workingHours("Lunedì - Venerdì: 08:00 - 16:00")
                    .build();
            doctorRepository.save(corti);
            userRepository.save(AppUser.builder()
                    .email(corti.getEmail())
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.DOCTOR)
                    .build());

            log.info("Medici clinici creati con credenziali.");
        }

        // Popola support user
        if (userRepository.findByEmail("support@healthcare.com").isEmpty()) {
            userRepository.save(AppUser.builder()
                    .email("support@healthcare.com")
                    .password(passwordEncoder.encode("support123"))
                    .role(Role.SUPPORT)
                    .build());
            log.info("Utente Supporto creato.");
        }
    }
}
