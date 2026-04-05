package com.example.ddchathub.service;

import com.example.ddchathub.dto.LoginResponse;
import com.example.ddchathub.dto.RegisterRequest;
import com.example.ddchathub.entity.Admin;
import com.example.ddchathub.entity.Role;
import com.example.ddchathub.repository.AdminRepository;
import com.example.ddchathub.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (adminRepository.findByUsername(request.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username นี้มีผู้ใช้งานแล้ว");
        }

        Role assignedRole = adminRepository.count() == 0 ? Role.SUPER_ADMIN : Role.STAFF;

        Admin admin = Admin.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(assignedRole)
                .build();

        adminRepository.save(admin);

        // 4. สร้าง Token ส่งกลับไปให้เลย จะได้ Login อัตโนมัติหลังสมัครเสร็จ
        String jwtToken = jwtService.generateToken(admin.getUsername());
        return new LoginResponse(jwtToken);
    }
}