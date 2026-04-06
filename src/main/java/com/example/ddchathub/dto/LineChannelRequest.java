package com.example.ddchathub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LineChannelRequest(

        @NotBlank(message = "ชื่อ LINE OA ห้ามเป็นค่าว่าง")
        @Size(max = 100, message = "ชื่อ LINE OA ต้องไม่เกิน 100 ตัวอักษร")
        String channelName,

        @NotBlank(message = "Channel Access Token ห้ามเป็นค่าว่าง")
        String channelAccessToken,

        // ไม่บังคับ — บางคนอาจยังไม่มี secret
        String channelSecret,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
                message = "รูปแบบรหัสสีไม่ถูกต้อง (เช่น #06C755)")
        String colorCode

) {}