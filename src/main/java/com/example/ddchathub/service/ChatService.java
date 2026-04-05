package com.example.ddchathub.service;

import com.example.ddchathub.dto.TagRequest;
import com.example.ddchathub.entity.Customer;
import com.example.ddchathub.entity.LineChannel;
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
import java.util.stream.Collectors;

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

        // 💡 1. ค้นหาข้อความล่าสุด เพื่อดูว่าลูกค้าคุยค้างไว้กับ "สาขาไหน"
        Message lastMsg = messageRepository.findTopByCustomerIdOrderByCreatedAtDesc(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "ยังไม่มีประวัติการคุย ไม่สามารถตอบกลับได้"));

        LineChannel channel = lastMsg.getLineChannel();
        if (channel == null || channel.getChannelAccessToken() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ไม่ได้ตั้งค่า Token ของสาขานี้ไว้");
        }

        try {
            MessagingApiClient dynamicClient = MessagingApiClient.builder(channel.getChannelAccessToken()).build();

            // 3. ส่งข้อความกลับไปหา LINE
            PushMessageRequest pushRequest = new PushMessageRequest(
                    customer.getLineUserId(),
                    List.of(new TextMessage(text)),
                    false, null
            );

            dynamicClient.pushMessage(UUID.randomUUID(), pushRequest).get();

            // 4. บันทึกข้อความลง Database แอดมินตอบกลับ
            Message adminMessage = Message.builder()
                    .customer(customer)
                    .lineChannel(channel) // ผูกว่าเป็นแชทของสาขานี้
                    .senderType("AGENT")
                    .content(text)
                    .build();
            Message savedMsg = messageRepository.save(adminMessage);

            // 5. อัปเดตหน้าจอแอดมินคนอื่นๆ
            messagingTemplate.convertAndSend("/topic/messages", savedMsg);

            log.info("✅ ส่งข้อความตอบกลับในนามสาขา {} สำเร็จ", channel.getChannelName());

        } catch (Exception e) {
            log.error("❌ ส่งข้อความไม่สำเร็จ: ", e);
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


            List<Map<String, Object>> tagsList = new ArrayList<>();
            if (c.getTags() != null) {
                c.getTags().forEach(tag -> {
                    Map<String, Object> tagMap = new HashMap<>();
                    tagMap.put("id", tag.getId());
                    tagMap.put("name", tag.getName());
                    // ⚠️ หมายเหตุ: ถ้า Entity ของคุณชื่อ colorCode ให้เปลี่ยนเป็น tag.getColorCode() นะครับ
                    tagMap.put("color", tag.getColorCode());
                    tagsList.add(tagMap);
                });
            }
            // 💡 2. ยัด List ของแท็กใส่ลงไปในกล่องของลูกค้าคนนี้
            map.put("tags", tagsList);

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

            map.put("unread", 0); // ปล่อย 0 ไว้ก่อน
            summaries.add(map);
        }

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