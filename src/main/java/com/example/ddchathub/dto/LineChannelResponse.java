package com.example.ddchathub.dto;

import java.util.UUID;

// ⚠️ ไม่มี channelAccessToken และ channelSecret โดยเจตนา — ห้ามส่งออก
public record LineChannelResponse(
        UUID id,
        String channelName,
        String colorCode,
        // แสดงว่าตั้งค่า token ไว้แล้วหรือยัง โดยไม่เปิดเผยค่าจริง
        boolean hasToken,
        boolean hasSecret
) {}