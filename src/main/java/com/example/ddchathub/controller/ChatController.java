package com.example.ddchathub.controller;

import com.example.ddchathub.dto.ChatRequest;
import com.example.ddchathub.repository.MessageRepository;
import com.example.ddchathub.service.ChatService;
import com.linecorp.bot.messaging.model.Message;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final MessageRepository messageRepository;



    @GetMapping("/customers/{customerId}/messages")
    public ResponseEntity<List<com.example.ddchathub.entity.Message>> getChatHistory(
            @PathVariable UUID customerId) {

        List<com.example.ddchathub.entity.Message> history =
                messageRepository.findByCustomerIdOrderByCreatedAtAsc(customerId);

        return ResponseEntity.ok(history);
    }
    @PostMapping("/{customerId}/send")
    public ResponseEntity<Void> sendMessage(
            @PathVariable UUID customerId,
            @Valid @RequestBody ChatRequest request) {

        chatService.sendReplyToCustomer(customerId, request.text(), request.senderName());

        return ResponseEntity.ok().build();
    }
    @GetMapping("/summary")
    public ResponseEntity<List<Map<String, Object>>> getChatSummaries() {
        List<Map<String, Object>> summaries = chatService.getChatSummaries();
        return ResponseEntity.ok(summaries);
    }

    @PutMapping("/customers/{customerId}/read")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable UUID customerId) {
        chatService.markAsRead(customerId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/customers/{customerId}/assign")
    public ResponseEntity<Void> assignAdmin(
            @PathVariable UUID customerId,
            @RequestParam(required = false) String adminName) { // ถ้าไม่ส่งมาแปลว่าปลดเคสออก
        chatService.assignAdmin(customerId, adminName);
        return ResponseEntity.ok().build();
    }
}