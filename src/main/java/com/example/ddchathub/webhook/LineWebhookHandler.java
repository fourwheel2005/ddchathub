package com.example.ddchathub.webhook;

import com.example.ddchathub.entity.Customer;
import com.example.ddchathub.entity.Message;
import com.example.ddchathub.repository.CustomerRepository;
import com.example.ddchathub.repository.MessageRepository;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.UserProfileResponse;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@LineMessageHandler
@RequiredArgsConstructor
public class LineWebhookHandler {

    private final CustomerRepository customerRepository;
    private final MessageRepository messageRepository;
    private final MessagingApiClient messagingApiClient;

    @EventMapping
    @Transactional
    public void handleTextMessageEvent(MessageEvent event) {
        if (event.message() instanceof TextMessageContent textMessage) {
            String lineUserId = event.source().userId();
            String messageText = textMessage.text();

            log.info("ได้รับข้อความ: '{}' จาก userID: {}", messageText, lineUserId);

            // 1. หาตัวลูกค้า หรือ สร้างใหม่ถ้ายังไม่มี
            Customer customer = customerRepository.findByLineUserId(lineUserId)
                    .orElseGet(() -> createNewCustomerFromLine(lineUserId));

            // 2. ถ้าดึงลูกค้าสำเร็จ (ไม่ null) ให้บันทึกข้อความลงฐานข้อมูล
            if (customer != null) {
                Message message = Message.builder()
                        .customer(customer)
                        .senderType("CUSTOMER") // ระบุว่าลูกค้าเป็นคนส่ง
                        .content(messageText)
                        .build();
                messageRepository.save(message);
                log.info("💾 บันทึกข้อความลง Database เรียบร้อย");
            }
        }
    }

    // 💡 เปลี่ยนจาก void เป็นการ return Customer
    private Customer createNewCustomerFromLine(String lineUserId) {
        try {
            UserProfileResponse userProfile = messagingApiClient.getProfile(lineUserId).get().body();

            if (userProfile == null) return null;

            Customer newCustomer = Customer.builder()
                    .lineUserId(lineUserId)
                    .fullName(userProfile.displayName())
                    .profilePictureUrl(userProfile.pictureUrl() != null ? userProfile.pictureUrl().toString() : null)
                    .build();

            log.info("🎉 สร้างโปรไฟล์ลูกค้าใหม่เรียบร้อย: {}", userProfile.displayName());
            return customerRepository.save(newCustomer);

        } catch (Exception e) {
            log.error("เกิดข้อผิดพลาดในการดึงข้อมูลโปรไฟล์ LINE: ", e);
            return null;
        }
    }
}