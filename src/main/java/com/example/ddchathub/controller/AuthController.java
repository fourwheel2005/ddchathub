package com.example.ddchathub.controller;

import com.example.ddchathub.dto.LoginRequest;
import com.example.ddchathub.dto.LoginResponse;
import com.example.ddchathub.dto.RegisterRequest;
import com.example.ddchathub.security.JwtService;
import com.example.ddchathub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService; // 💡 ดึง AuthService มาใช้

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String jwtToken = jwtService.generateToken(request.username());
        return ResponseEntity.ok(new LoginResponse(jwtToken));
    }
}