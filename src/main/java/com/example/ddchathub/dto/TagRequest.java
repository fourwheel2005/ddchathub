package com.example.ddchathub.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank(message = "ชื่อแท็กห้ามเป็นค่าว่าง")
        @Size(max = 50, message = "ชื่อแท็กต้องไม่เกิน 50 ตัวอักษร")
        String name,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "รูปแบบรหัสสีไม่ถูกต้อง (เช่น #FF0000)")
        String colorCode
) {}
