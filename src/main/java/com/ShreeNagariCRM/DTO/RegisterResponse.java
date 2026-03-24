package com.ShreeNagariCRM.DTO;

public record RegisterResponse(
        Long id,
        String email,
        String phone,
        String role,
        String message
) {}
