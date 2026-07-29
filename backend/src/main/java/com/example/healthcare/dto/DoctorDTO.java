package com.example.healthcare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record DoctorDTO(
    Long id,

    @NotBlank(message = "Il nome del medico è obbligatorio")
    String name,

    @NotBlank(message = "La specializzazione è obbligatoria")
    String specialization,

    @Email(message = "L'email deve essere valida")
    @NotBlank(message = "L'email è obbligatoria")
    String email
) {}
