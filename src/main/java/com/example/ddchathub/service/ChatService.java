package com.example.ddchathub.service;

import com.example.ddchathub.entity.Customer;
import com.example.ddchathub.entity.Message;
import com.example.ddchathub.repository.CustomerRepository;
import com.example.ddchathub.repository.MessageRepository;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.PushMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final CustomerRepository customerRepository;
    private final MessageRepository messageRepository;
    private final MessagingApiClient messagingApiClient;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendReplyToCustomer(UUID customerId, String text) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบข้อมูลลูกค้า"));

        if (customer.getLineUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ลูกค้ารายนี้ไม่ได้เชื่อมต่อกับ LINE");
        }

        try {
            // 1. สั่งให้ LINE ส่งข้อความหาลูกค้า
            PushMessageRequest pushMessageRequest = new PushMessageRequest(
                    customer.getLineUserId(),
                    List.of(new TextMessage(text)),
                    false,
                    null
            );

            messagingApiClient.pushMessage(UUID.randomUUID(), pushMessageRequest).get();

            // 2. บันทึกข้อความลง Database
            Message adminMessage = Message.builder()
                    .customer(customer)
                    .senderType("AGENT") // 💡 ใช้ 'AGENT' ให้ตรงกับที่หน้า React เช็กไว้ครับ
                    .content(text)
                    .createdAt(LocalDateTime.now()) // 💡 ประทับเวลาด้วย
                    .build();
            Message savedMessage = messageRepository.save(adminMessage);

            // 💡 3. ปาข้อความที่เพิ่งบันทึกเข้าท่อ WebSocket เพื่อให้แอดมินคนอื่นเห็นแบบ Real-time
            messagingTemplate.convertAndSend("/topic/messages", savedMessage);

            log.info("✅ ส่งข้อความหาลูกค้า {} สำเร็จ", customer.getFullName());

        } catch (Exception e) {
            log.error("ส่งข้อความไม่สำเร็จ: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ไม่สามารถส่งข้อความได้");
        }
    }
}