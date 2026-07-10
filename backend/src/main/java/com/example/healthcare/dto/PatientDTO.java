package com.example.healthcare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDTO {
    private Long id;

    @NotBlank(message = "Il nome è obbligatorio")
    private String name;

    @Email(message = "L'email deve essere valida")
    @NotBlank(message = "L'email è obbligatoria")
    private String email;

    private String phone;
}
