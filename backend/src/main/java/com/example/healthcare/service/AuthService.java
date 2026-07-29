package com.example.healthcare.service;

import com.example.healthcare.dto.LoginRequest;
import com.example.healthcare.dto.LoginResponse;
import com.example.healthcare.dto.RegistrationRequest;
import com.example.healthcare.dto.ChangePasswordRequest;
import com.example.healthcare.dto.TwoFactorSetupResponse;
import com.example.healthcare.dto.TwoFactorVerificationRequest;
import com.example.healthcare.dto.TwoFactorLoginRequest;
import com.example.healthcare.entity.AppUser;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.entity.Role;
import com.example.healthcare.repository.DoctorRepository;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.repository.UserRepository;
import com.example.healthcare.security.JwtUtils;
import com.example.healthcare.security.UserDetailsImpl;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final SecretGenerator secretGenerator;
    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;

    @Transactional
    public void registerPatient(RegistrationRequest registrationRequest) {
        if (userRepository.findByEmail(registrationRequest.email()).isPresent()) {
            throw new RuntimeException("Errore: L'email è già in uso!");
        }

        Patient patient = Patient.builder()
                .name(registrationRequest.name())
                .email(registrationRequest.email())
                .phone(registrationRequest.phone())
                .build();
        patientRepository.save(patient);

        AppUser user = AppUser.builder()
                .email(registrationRequest.email())
                .password(passwordEncoder.encode(registrationRequest.password()))
                .role(Role.PATIENT)
                .build();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        AppUser user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (user.is2faEnabled()) {
            return LoginResponse.builder()
                    .requires2fa(true)
                    .build();
        }

        return generateTokenResponse(authentication, user);
    }

    public LoginResponse verify2faAndLogin(TwoFactorLoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (!codeVerifier.isValidCode(user.getSecretKey(), request.code())) {
            throw new RuntimeException("Codice 2FA non valido");
        }

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return generateTokenResponse(authentication, user);
    }

    private LoginResponse generateTokenResponse(Authentication authentication, AppUser user) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority().replace("ROLE_", "");

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

        return LoginResponse.builder()
                .token(jwt)
                .email(userDetails.getEmail())
                .role(role)
                .profileId(profileId)
                .build();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new RuntimeException("La vecchia password non è corretta");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public TwoFactorSetupResponse setup2fa() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        String secret = secretGenerator.generate();
        user.setSecretKey(secret);
        userRepository.save(user);

        QrData data = qrDataFactory.newBuilder()
                .label(email)
                .secret(secret)
                .issuer("HealthCare Plus")
                .build();

        try {
            String qrCodeImage = Utils.getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
            return new TwoFactorSetupResponse(secret, qrCodeImage);
        } catch (QrGenerationException e) {
            throw new RuntimeException("Errore durante la generazione del QR Code", e);
        }
    }

    @Transactional
    public void enable2fa(TwoFactorVerificationRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (!codeVerifier.isValidCode(user.getSecretKey(), request.code())) {
            throw new RuntimeException("Codice 2FA non valido. Riprova.");
        }

        user.set2faEnabled(true);
        userRepository.save(user);
    }
}
