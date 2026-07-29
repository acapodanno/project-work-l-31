package com.example.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TwoFactorVerificationRequest(
    @NotBlank(message = "Il codice è obbligatorio")
    String code
) {}
