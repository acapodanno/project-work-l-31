package com.example.healthcare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Il nome del medico è obbligatorio")
    private String name;

    @NotBlank(message = "La specializzazione è obbligatoria")
    private String specialization;

    @Email(message = "L'email deve essere valida")
    @NotBlank(message = "L'email è obbligatoria")
    @Column(unique = true)
    private String email;

    private Integer experienceYears;

    @Column(length = 1000)
    private String bio;

    private String workingHours;
}
