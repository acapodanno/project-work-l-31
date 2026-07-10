package com.example.healthcare.controller;

import com.example.healthcare.dto.LoginRequest;
import com.example.healthcare.dto.LoginResponse;
import com.example.healthcare.dto.RegistrationRequest;
import com.example.healthcare.entity.AppUser;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.entity.Role;
import com.example.healthcare.repository.DoctorRepository;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.repository.UserRepository;
import com.example.healthcare.security.JwtUtils;
import com.example.healthcare.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        Long profileId = null;
        if ("PATIENT".equals(role)) {
            profileId = patientRepository.findByEmail(userDetails.getEmail())
                    .map(Patient::getId)
                    .orElse(null);
        } else if ("DOCTOR".equals(role)) {
            profileId = doctorRepository.findByEmail(userDetails.getEmail())
                    .map(com.example.healthcare.entity.Doctor::getId)
                    .orElse(null);
        }

        return ResponseEntity.ok(LoginResponse.builder()
                .token(jwt)
                .email(userDetails.getEmail())
                .role(role)
                .profileId(profileId)
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Errore: L'email è già in uso!"));
        }

        // Create new patient profile
        Patient patient = Patient.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .phone(registrationRequest.getPhone())
                .build();
        patientRepository.save(patient);

        // Create user credentials
        AppUser user = AppUser.builder()
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .role(Role.PATIENT)
                .build();
        userRepository.save(user);

        return new ResponseEntity<>(Map.of("message", "Paziente registrato con successo!"), HttpStatus.CREATED);
    }
}
