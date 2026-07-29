package com.example.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ChangePasswordRequest(
    @NotBlank(message = "La vecchia password è obbligatoria")
    String oldPassword,

    @NotBlank(message = "La nuova password è obbligatoria")
    String newPassword
) {}
