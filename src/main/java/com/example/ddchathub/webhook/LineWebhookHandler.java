package com.example.ddchathub.webhook;

import com.example.ddchathub.entity.Customer;
import com.example.ddchathub.entity.LineChannel;
import com.example.ddchathub.entity.Message;
import com.example.ddchathub.repository.CustomerRepository;
import com.example.ddchathub.repository.LineChannelRepository;
import com.example.ddchathub.repository.MessageRepository;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.UserProfileResponse;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@LineMessageHandler
@RequiredArgsConstructor
public class LineWebhookHandler {

    private final CustomerRepository customerRepository;
    private final MessageRepository messageRepository;
    private final MessagingApiClient messagingApiClient;
    private final LineChannelRepository lineChannelRepository;

    private final SimpMessagingTemplate messagingTemplate;

    @Value("${line.bot.destination-id}")
    private String destinationId;

    @EventMapping
    @Transactional
    public void handleTextMessageEvent(MessageEvent event) {
        if (event.message() instanceof TextMessageContent textMessage) {
            String lineUserId = event.source().userId();
            String messageText = textMessage.text();

            // 1. หาหรือสร้างลูกค้า
            Customer customer = customerRepository.findByLineUserId(lineUserId)
                    .orElseGet(() -> createNewCustomerFromLine(lineUserId));

            // 2. หาว่าทักมาที่ LINE OA ตัวไหน โดยใช้ค่าจาก config แทน
            LineChannel channel = lineChannelRepository.findByLineDestinationId(destinationId)
                    .orElseGet(() -> {
                        LineChannel newChannel = new LineChannel();
                        newChannel.setLineDestinationId(destinationId);
                        newChannel.setChannelName("ช่องทางใหม่");
                        return lineChannelRepository.save(newChannel);
                    });

            // 3. บันทึกข้อความลง Database
            Message message = Message.builder()
                    .customer(customer)
                    .lineChannel(channel)
                    .content(messageText)
                    .senderType("CUSTOMER")
                    .createdAt(LocalDateTime.now())
                    .build();
            messageRepository.save(message);


            messagingTemplate.convertAndSend("/topic/messages", message);

            log.info("📢 ส่งข้อความเข้า WebSocket เรียบร้อย!");
        }
    }

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
            // 💡 3. เพิ่มการ Log Error ให้ชัดเจนขึ้นว่าพังที่ใคร
            log.error("เกิดข้อผิดพลาดในการดึงข้อมูลโปรไฟล์ LINE (userID: {}): {}", lineUserId, e.getMessage());
            return null;
        }
    }
}