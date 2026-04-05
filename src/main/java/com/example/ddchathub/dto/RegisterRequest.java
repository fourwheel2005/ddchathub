package com.example.ddchathub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username ห้ามเป็นค่าว่าง")
        String username,

        @NotBlank(message = "Password ห้ามเป็นค่าว่าง")
        @Size(min = 6, message = "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร")
        String password
) {}