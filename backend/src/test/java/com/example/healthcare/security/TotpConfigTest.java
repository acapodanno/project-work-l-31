package com.example.healthcare.security;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TotpConfigTest {

    private final TotpConfig totpConfig = new TotpConfig();

    @Test
    void secretGenerator() {
        SecretGenerator generator = totpConfig.secretGenerator();
        assertNotNull(generator);
    }

    @Test
    void qrDataFactory() {
        QrDataFactory factory = totpConfig.qrDataFactory();
        assertNotNull(factory);
    }

    @Test
    void qrGenerator() {
        QrGenerator generator = totpConfig.qrGenerator();
        assertNotNull(generator);
    }

    @Test
    void codeVerifier() {
        CodeVerifier verifier = totpConfig.codeVerifier();
        assertNotNull(verifier);
    }
}
