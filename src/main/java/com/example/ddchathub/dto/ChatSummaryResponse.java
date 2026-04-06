package com.example.ddchathub.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ChatSummaryResponse {
    private UUID id;
    private String name;
    private String lastMessage;
    private LocalDateTime time;
    private int unread;
    private String channel;
    private UUID channelId;
    private String channelColor;

    // 💡 สิ่งที่หายไป! ต้องเพิ่ม List ของ Tag เข้าไปด้วยครับ
    private List<TagRequest> tags;
}