package com.example.healthcare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDTO {
    private Long id;

    @NotBlank(message = "Il nome del medico è obbligatorio")
    private String name;

    @NotBlank(message = "La specializzazione è obbligatoria")
    private String specialization;

    @Email(message = "L'email deve essere valida")
    @NotBlank(message = "L'email è obbligatoria")
    private String email;
}
