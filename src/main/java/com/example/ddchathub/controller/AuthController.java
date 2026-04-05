package com.example.ddchathub.controller;

import com.example.ddchathub.dto.LoginRequest;
import com.example.ddchathub.dto.LoginResponse;
import com.example.ddchathub.dto.RegisterRequest;
import com.example.ddchathub.entity.Admin;
import com.example.ddchathub.repository.AdminRepository;
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
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService; // 💡 ดึง AuthService มาใช้
    private final AdminRepository adminRepository;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1. ตรวจสอบ Username / Password ตามปกติ
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        Admin admin = adminRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("ไม่พบผู้ใช้งาน"));

        String jwtToken = jwtService.generateToken(admin);

        return ResponseEntity.ok(new LoginResponse(jwtToken));
    }
}