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
import java.util.*;

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

    public List<Map<String, Object>> getChatSummaries() {
        List<Customer> customers = customerRepository.findAll();
        List<Map<String, Object>> summaries = new ArrayList<>();

        for (Customer c : customers) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getFullName() != null ? c.getFullName() : "ลูกค้าใหม่");

            // หาข้อความล่าสุด
            Optional<Message> lastMsg = messageRepository.findTopByCustomerIdOrderByCreatedAtDesc(c.getId());

            if (lastMsg.isPresent()) {
                map.put("lastMessage", lastMsg.get().getContent());
                map.put("time", lastMsg.get().getCreatedAt());

                // ดึงชื่อสาขา (ถ้ามี)
                if(lastMsg.get().getLineChannel() != null) {
                    map.put("channel", lastMsg.get().getLineChannel().getChannelName());
                } else {
                    map.put("channel", "LINE OA");
                }
            } else {
                map.put("lastMessage", "ยังไม่มีข้อความ");
                map.put("time", null);
                map.put("channel", "LINE OA");
            }

            map.put("unread", 0); // 💡 ปล่อย 0 ไว้ก่อน เดี๋ยวเราทำระบบอ่านแล้วในสเต็ป 2
            summaries.add(map);
        }

        // เรียงลำดับให้คนที่ทักมาล่าสุดอยู่บนสุด
        summaries.sort((m1, m2) -> {
            LocalDateTime t1 = (LocalDateTime) m1.get("time");
            LocalDateTime t2 = (LocalDateTime) m2.get("time");
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t2.compareTo(t1); // มากไปน้อย (ใหม่ไปเก่า)
        });

        return summaries;
    }
}