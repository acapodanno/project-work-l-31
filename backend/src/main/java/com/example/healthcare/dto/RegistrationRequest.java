package com.example.healthcare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RegistrationRequest(
    @NotBlank(message = "Il nome è obbligatorio")
    String name,

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "L'email deve essere valida")
    String email,

    @NotBlank(message = "La password è obbligatoria")
    String password,

    String phone
) {}
