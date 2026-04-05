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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final CustomerRepository customerRepository;
    private final MessageRepository messageRepository;
    private final MessagingApiClient messagingApiClient;

    @Transactional
    public void sendReplyToCustomer(UUID customerId, String text) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบข้อมูลลูกค้า"));

        if (customer.getLineUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ลูกค้ารายนี้ไม่ได้เชื่อมต่อกับ LINE");
        }

        try {
            // 1. สั่งให้ LINE ส่งข้อความหาลูกค้า (ใช้รูปแบบใหม่ของ SDK 8.4.0)
            PushMessageRequest pushMessageRequest = new PushMessageRequest(
                    customer.getLineUserId(),
                    List.of(new TextMessage(text)), // ส่งเป็น TextMessage
                    false, // แจ้งเตือนปกติ
                    null
            );

            messagingApiClient.pushMessage(UUID.randomUUID(), pushMessageRequest).get();

            // 2. บันทึกข้อความที่แอดมินส่ง ลงใน Database ของเรา
            Message adminMessage = Message.builder()
                    .customer(customer)
                    .senderType("ADMIN") // ระบุว่าแอดมินเป็นคนตอบ
                    .content(text)
                    .build();
            messageRepository.save(adminMessage);

            log.info("✅ ส่งข้อความหาลูกค้า {} สำเร็จ", customer.getFullName());

        } catch (Exception e) {
            log.error("ส่งข้อความไม่สำเร็จ: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ไม่สามารถส่งข้อความได้");
        }
    }
}