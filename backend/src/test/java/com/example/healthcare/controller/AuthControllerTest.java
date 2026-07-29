package com.example.healthcare.controller;

import com.example.healthcare.dto.LoginRequest;
import com.example.healthcare.dto.LoginResponse;
import com.example.healthcare.dto.RegistrationRequest;
import com.example.healthcare.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.example.healthcare.security.JwtUtils jwtUtils;

    @MockBean
    private com.example.healthcare.security.UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAuthenticateUser() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        LoginResponse response = LoginResponse.builder()
                .token("mock-jwt-token")
                .email("test@example.com")
                .role("PATIENT")
                .profileId(1L)
                .requires2fa(false)
                .build();

        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testRegisterPatient_Success() throws Exception {
        RegistrationRequest request = RegistrationRequest.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password")
                .phone("123456789")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Paziente registrato con successo!"));
    }

    @Test
    void testRegisterPatient_Failure() throws Exception {
        RegistrationRequest request = RegistrationRequest.builder()
                .name("Test User")
                .email("existing@example.com")
                .password("password")
                .phone("123456789")
                .build();

        doThrow(new RuntimeException("Email già in uso"))
                .when(authService).registerPatient(any(RegistrationRequest.class));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email già in uso"));
    }
}
