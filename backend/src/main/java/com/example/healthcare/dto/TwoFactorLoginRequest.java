package com.example.healthcare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TwoFactorLoginRequest(
    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "L'email deve essere valida")
    String email,

    @NotBlank(message = "Il codice è obbligatorio")
    String code
) {}
