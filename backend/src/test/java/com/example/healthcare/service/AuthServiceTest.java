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
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private SecretGenerator secretGenerator;
    @Mock private QrDataFactory qrDataFactory;
    @Mock private QrGenerator qrGenerator;
    @Mock private CodeVerifier codeVerifier;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerPatient_Success() {
        RegistrationRequest req = new RegistrationRequest("John", "john@example.com", "pass", "123");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded_pass");

        authService.registerPatient(req);

        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(userRepository, times(1)).save(any(AppUser.class));
    }

    @Test
    void registerPatient_EmailAlreadyInUse() {
        RegistrationRequest req = new RegistrationRequest("John", "john@example.com", "pass", "123");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(new AppUser()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.registerPatient(req));
        assertEquals("Errore: L'email è già in uso!", exception.getMessage());
    }

    @Test
    void authenticateUser_SuccessWithout2FA() {
        LoginRequest req = new LoginRequest("john@example.com", "pass");
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        AppUser user = AppUser.builder().email("john@example.com").role(Role.PATIENT).is2faEnabled(false).build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "john@example.com", "pass", new SimpleGrantedAuthority("ROLE_PATIENT"));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt_token");

        Patient patient = new Patient();
        patient.setId(1L);
        when(patientRepository.findByEmail("john@example.com")).thenReturn(Optional.of(patient));

        LoginResponse response = authService.authenticateUser(req);

        assertFalse(response.requires2fa());
        assertEquals("jwt_token", response.token());
        assertEquals("PATIENT", response.role());
        assertEquals(1L, response.profileId());
    }

    @Test
    void authenticateUser_Requires2FA() {
        LoginRequest req = new LoginRequest("john@example.com", "pass");
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        AppUser user = AppUser.builder().email("john@example.com").is2faEnabled(true).build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        LoginResponse response = authService.authenticateUser(req);

        assertTrue(response.requires2fa());
        assertNull(response.token());
    }

    @Test
    void verify2faAndLogin_Success() {
        TwoFactorLoginRequest req = new TwoFactorLoginRequest("john@example.com", "123456");
        AppUser user = AppUser.builder().email("john@example.com").secretKey("secret").role(Role.PATIENT).build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(codeVerifier.isValidCode("secret", "123456")).thenReturn(true);

        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("jwt_token");

        Patient patient = new Patient();
        patient.setId(1L);
        when(patientRepository.findByEmail("john@example.com")).thenReturn(Optional.of(patient));

        LoginResponse response = authService.verify2faAndLogin(req);

        assertEquals("jwt_token", response.token());
        assertEquals("PATIENT", response.role());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void verify2faAndLogin_InvalidCode() {
        TwoFactorLoginRequest req = new TwoFactorLoginRequest("john@example.com", "123456");
        AppUser user = AppUser.builder().email("john@example.com").secretKey("secret").build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(codeVerifier.isValidCode("secret", "123456")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.verify2faAndLogin(req));
        assertEquals("Codice 2FA non valido", exception.getMessage());
    }

    @Test
    void changePassword_Success() {
        Authentication auth = new UsernamePasswordAuthenticationToken("john@example.com", "pass");
        SecurityContextHolder.getContext().setAuthentication(auth);

        ChangePasswordRequest req = new ChangePasswordRequest("old_pass", "new_pass");
        AppUser user = AppUser.builder().email("john@example.com").password("encoded_old").build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old_pass", "encoded_old")).thenReturn(true);
        when(passwordEncoder.encode("new_pass")).thenReturn("encoded_new");

        authService.changePassword(req);

        assertEquals("encoded_new", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_WrongOldPassword() {
        Authentication auth = new UsernamePasswordAuthenticationToken("john@example.com", "pass");
        SecurityContextHolder.getContext().setAuthentication(auth);

        ChangePasswordRequest req = new ChangePasswordRequest("wrong_pass", "new_pass");
        AppUser user = AppUser.builder().email("john@example.com").password("encoded_old").build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_pass", "encoded_old")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.changePassword(req));
        assertEquals("La vecchia password non è corretta", exception.getMessage());
    }

    @Test
    void setup2fa_Success() throws QrGenerationException {
        Authentication auth = new UsernamePasswordAuthenticationToken("john@example.com", "pass");
        SecurityContextHolder.getContext().setAuthentication(auth);

        AppUser user = AppUser.builder().email("john@example.com").build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(secretGenerator.generate()).thenReturn("secret");

        QrData.Builder qrDataBuilder = mock(QrData.Builder.class);
        when(qrDataFactory.newBuilder()).thenReturn(qrDataBuilder);
        when(qrDataBuilder.label(anyString())).thenReturn(qrDataBuilder);
        when(qrDataBuilder.secret(anyString())).thenReturn(qrDataBuilder);
        when(qrDataBuilder.issuer(anyString())).thenReturn(qrDataBuilder);
        QrData qrData = new QrData.Builder().label("john@example.com").secret("secret").issuer("test").build();
        when(qrDataBuilder.build()).thenReturn(qrData);

        when(qrGenerator.generate(qrData)).thenReturn(new byte[]{1, 2, 3});
        when(qrGenerator.getImageMimeType()).thenReturn("image/png");

        TwoFactorSetupResponse res = authService.setup2fa();

        assertEquals("secret", res.secret());
        assertTrue(res.qrCodeImageBase64().startsWith("data:image/png;base64,"));
        verify(userRepository).save(user);
    }

    @Test
    void enable2fa_Success() {
        Authentication auth = new UsernamePasswordAuthenticationToken("john@example.com", "pass");
        SecurityContextHolder.getContext().setAuthentication(auth);

        TwoFactorVerificationRequest req = new TwoFactorVerificationRequest("123456");
        AppUser user = AppUser.builder().email("john@example.com").secretKey("secret").is2faEnabled(false).build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(codeVerifier.isValidCode("secret", "123456")).thenReturn(true);

        authService.enable2fa(req);

        assertTrue(user.is2faEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void enable2fa_InvalidCode() {
        Authentication auth = new UsernamePasswordAuthenticationToken("john@example.com", "pass");
        SecurityContextHolder.getContext().setAuthentication(auth);

        TwoFactorVerificationRequest req = new TwoFactorVerificationRequest("123456");
        AppUser user = AppUser.builder().email("john@example.com").secretKey("secret").is2faEnabled(false).build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(codeVerifier.isValidCode("secret", "123456")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.enable2fa(req));
        assertEquals("Codice 2FA non valido. Riprova.", exception.getMessage());
        assertFalse(user.is2faEnabled());
    }
}
