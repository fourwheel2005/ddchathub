// สร้างไฟล์ใหม่ชื่อ ChatPresenceController.java
package com.example.ddchathub.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatPresenceController {

    // เมื่อ React ยิงมาที่ /app/chat.presence
    @MessageMapping("/chat.presence")
    // จะเด้งกระจายออกไปที่ /topic/presence ให้ทุกคนรู้
    @SendTo("/topic/presence")
    public String announcePresence(String payload) {
        return payload; // ส่ง JSON ที่ React แนบมา กระจายให้คนอื่นเลย
    }
}