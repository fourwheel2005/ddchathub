package com.example.ddchathub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CustomerRequest(
        @NotBlank(message = "ชื่อลูกค้าห้ามเป็นค่าว่าง")
        String fullName,

        // ดักเบอร์โทรให้มีเฉพาะตัวเลข 10 หลัก (เบอร์มือถือไทย)
        @NotBlank(message = "เบอร์โทรศัพท์ห้ามเป็นค่าว่าง")
        @Pattern(regexp = "^[0-9]{10}$", message = "รูปแบบเบอร์โทรศัพท์ไม่ถูกต้อง (ต้องเป็นตัวเลข 10 หลัก)")
        String phoneNumber
) {}