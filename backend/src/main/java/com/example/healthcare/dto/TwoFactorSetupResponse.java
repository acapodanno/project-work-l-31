package com.example.healthcare.dto;

import lombok.Builder;

@Builder
public record TwoFactorSetupResponse(
    String secret,
    String qrCodeImageBase64
) {}
