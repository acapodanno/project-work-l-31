package com.example.healthcare.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String testSecret = "TestSecretKeyForHealthcarePlusAppMustBeAtLeast32BytesLongForSecurityReasons123";
    private final int testExpirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", testExpirationMs);
    }

    @Test
    void testGenerateAndValidateJwtToken() {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L, 
                "test@example.com", 
                "password", 
                new SimpleGrantedAuthority("ROLE_PATIENT")
        );
        
        Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        
        String token = jwtUtils.generateJwtToken(authentication);
        
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("test@example.com", jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    void testValidateJwtToken_InvalidToken() {
        assertFalse(jwtUtils.validateJwtToken("invalid.token.string"));
    }
}
