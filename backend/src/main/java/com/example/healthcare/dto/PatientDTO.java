package com.example.healthcare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PatientDTO(
    Long id,

    @NotBlank(message = "Il nome è obbligatorio")
    String name,

    @Email(message = "L'email deve essere valida")
    @NotBlank(message = "L'email è obbligatoria")
    String email,

    String phone
) {}
