package com.example.healthcare.controller;

import com.example.healthcare.dto.LoginRequest;
import com.example.healthcare.dto.LoginResponse;
import com.example.healthcare.dto.RegistrationRequest;
import com.example.healthcare.dto.ChangePasswordRequest;
import com.example.healthcare.dto.TwoFactorSetupResponse;
import com.example.healthcare.dto.TwoFactorVerificationRequest;
import com.example.healthcare.dto.TwoFactorLoginRequest;
import com.example.healthcare.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/login/verify-2fa")
    public ResponseEntity<LoginResponse> verify2faAndLogin(@Valid @RequestBody TwoFactorLoginRequest request) {
        return ResponseEntity.ok(authService.verify2faAndLogin(request));
    }

    @GetMapping("/2fa/setup")
    public ResponseEntity<TwoFactorSetupResponse> setup2fa() {
        return ResponseEntity.ok(authService.setup2fa());
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<?> enable2fa(@Valid @RequestBody TwoFactorVerificationRequest request) {
        authService.enable2fa(request);
        return ResponseEntity.ok(Map.of("message", "Autenticazione a due fattori abilitata con successo"));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "Password aggiornata con successo"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody RegistrationRequest registrationRequest) {
        try {
            authService.registerPatient(registrationRequest);
            return new ResponseEntity<>(Map.of("message", "Paziente registrato con successo!"), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
