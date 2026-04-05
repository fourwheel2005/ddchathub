package com.example.ddchathub.controller;

import com.example.ddchathub.dto.ChatRequest;
import com.example.ddchathub.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/{customerId}/send")
    public ResponseEntity<Void> sendMessage(
            @PathVariable UUID customerId,
            @Valid @RequestBody ChatRequest request) {

        chatService.sendReplyToCustomer(customerId, request.text());
        return ResponseEntity.ok().build();
    }
}