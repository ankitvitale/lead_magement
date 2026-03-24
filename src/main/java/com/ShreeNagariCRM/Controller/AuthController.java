package com.ShreeNagariCRM.Controller;

import com.ShreeNagariCRM.DTO.LoginRequest;
import com.ShreeNagariCRM.DTO.LoginResponse;
import com.ShreeNagariCRM.DTO.RegisterRequest;
import com.ShreeNagariCRM.DTO.RegisterResponse;
import com.ShreeNagariCRM.Entity.User;
import com.ShreeNagariCRM.Repository.UserRepository;
import com.ShreeNagariCRM.Security.JwtUtil;
import com.ShreeNagariCRM.Service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public AuthController(UserRepository repository,
                          PasswordEncoder encoder,
                          JwtUtil jwtUtil,AuthService authService) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.authService=authService;
    }

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        User user = repository.findByEmailOrPhone(request.identifier())
                .orElseThrow(() -> new RuntimeException("Invalid email or phone"));

        if (!encoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user);

        return new LoginResponse(token, user.getRole().name());
    }
}
