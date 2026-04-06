package com.example.ddchathub.controller;

import com.example.ddchathub.entity.Customer;
import com.example.ddchathub.entity.LineChannel;
import com.example.ddchathub.entity.Message;
import com.example.ddchathub.repository.CustomerRepository;
import com.example.ddchathub.repository.LineChannelRepository;
import com.example.ddchathub.repository.MessageRepository;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.UserProfileResponse;
import com.linecorp.bot.webhook.model.CallbackRequest;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
public class MultiLineWebhookController {

    private final LineChannelRepository lineChannelRepository;
    private final CustomerRepository customerRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // 💡 1. เปลี่ยนรับค่าเป็น UUID ให้ตรงกับ Database ของคุณ
    @PostMapping("/{channelId}")
    public ResponseEntity<Void> receiveWebhook(
            @PathVariable UUID channelId,
            @RequestBody String payload) {

        LineChannel channel = lineChannelRepository.findById(channelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบข้อมูลสาขา ID: " + channelId));

        try {
            CallbackRequest callbackRequest = objectMapper.readValue(payload, CallbackRequest.class);
            // 💡 2. สร้างตัวยิง API เพื่อดึงโปรไฟล์ลูกค้า โดยใช้ Token ของสาขานี้
            MessagingApiClient dynamicClient = MessagingApiClient.builder(channel.getChannelAccessToken()).build();

            callbackRequest.events().forEach(event -> {
                if (event instanceof MessageEvent messageEvent && messageEvent.message() instanceof TextMessageContent textMessage) {

                    String lineUserId = messageEvent.source().userId();
                    String text = textMessage.text();

                    // 💡 3. หาข้อมูลลูกค้า หรือสร้างใหม่พร้อมดึงรูปโปรไฟล์ และ 🎯 "ผูกสาขา" 🎯
                    Customer customer = customerRepository.findByLineUserId(lineUserId)
                            .orElseGet(() -> {
                                Customer newCust = new Customer();
                                newCust.setLineUserId(lineUserId);

                                // 🎯 จุดที่แก้ไข: ผูกลูกค้าคนนี้เข้ากับสาขา (Channel) ที่รับข้อความมา!
                                newCust.setLineChannel(channel);

                                try {
                                    // ดึงข้อมูลจาก LINE สดๆ
                                    UserProfileResponse profile = dynamicClient.getProfile(lineUserId).get().body();
                                    if (profile != null) {
                                        newCust.setFullName(profile.displayName());
                                        newCust.setProfilePictureUrl(profile.pictureUrl() != null ? profile.pictureUrl().toString() : null);
                                    }
                                } catch (Exception e) {
                                    log.error("ดึงโปรไฟล์ไม่ได้: {}", e.getMessage());
                                    newCust.setFullName("ลูกค้าใหม่");
                                }
                                return customerRepository.save(newCust); // บันทึกลง Database
                            });

                    // บันทึกข้อความลง Database
                    Message message = Message.builder()
                            .customer(customer)
                            .lineChannel(channel)
                            .content(text)
                            .senderType("CUSTOMER")
                            .build();

                    Message savedMessage = messageRepository.save(message);

                    // ปาเข้าหน้าจอเว็บแชท
                    messagingTemplate.convertAndSend("/topic/messages", savedMessage);
                    log.info("📢 [สาขา: {}] {} พิมพ์มาว่า: {}", channel.getChannelName(), customer.getFullName(), text);
                }
            });
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("❌ ประมวลผล Webhook ผิดพลาด: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}