package com.example.healthcare.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
    String token,
    String email,
    String role,
    Long profileId,
    boolean requires2fa
) {}
