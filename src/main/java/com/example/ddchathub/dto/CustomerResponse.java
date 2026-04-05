package com.example.ddchathub.dto;

import java.util.Set;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String fullName,
        String phoneNumber,
        Set<TagResponse> tags // 💡 แนบข้อมูลแท็ก (TagResponse ที่เราสร้างไว้ใน Step 1) กลับไปด้วยเลย
) {}