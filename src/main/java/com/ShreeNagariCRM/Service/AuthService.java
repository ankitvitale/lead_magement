package com.ShreeNagariCRM.Service;

import com.ShreeNagariCRM.DTO.RegisterRequest;
import com.ShreeNagariCRM.DTO.RegisterResponse;
import com.ShreeNagariCRM.Entity.User;
import com.ShreeNagariCRM.Entity.enums.Role;
import com.ShreeNagariCRM.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository repository,
                       PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public RegisterResponse register(RegisterRequest request) {

        if ((request.email() == null || request.email().isBlank()) &&
            (request.phone() == null || request.phone().isBlank())) {
            throw new RuntimeException("Email or Phone is required");
        }

        if (request.email() != null && repository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already registered");
        }

        if (request.phone() != null && repository.existsByPhone(request.phone())) {
            throw new RuntimeException("Phone already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPassword(encoder.encode(request.password()));
        user.setRole(request.role() == null ? Role.USER : request.role());

        User savedUser = repository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getRole().name(),
                "User registered successfully"
        );
    }
}
