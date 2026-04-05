package com.example.ddchathub.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "ข้อความห้ามเป็นค่าว่าง")
        String text
) {}