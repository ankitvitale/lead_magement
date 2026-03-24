package com.ShreeNagariCRM.DTO;

import com.ShreeNagariCRM.Entity.enums.Role;

public record RegisterRequest(
        String email,
        String phone,
        String password,
        Role role
) {}
