package com.example.ddchathub.service;

import com.example.ddchathub.dto.LineChannelRequest;
import com.example.ddchathub.dto.LineChannelResponse;
import com.example.ddchathub.entity.LineChannel;
import com.example.ddchathub.repository.LineChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LineChannelService {

    private final LineChannelRepository lineChannelRepository;

    // ดึงรายการ LINE OA ทั้งหมด (ไม่เปิด token)
    @Transactional(readOnly = true)
    public List<LineChannelResponse> getAll() {
        return lineChannelRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // เพิ่ม LINE OA ใหม่
    @Transactional
    public LineChannelResponse create(LineChannelRequest request) {
        // ป้องกันชื่อซ้ำ — ชื่อสาขาต้องไม่ซ้ำกัน
        if (lineChannelRepository.existsByChannelName(request.channelName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "มี LINE OA ชื่อ \"" + request.channelName() + "\" อยู่แล้ว");
        }

        LineChannel channel = LineChannel.builder()
                .channelName(request.channelName())
                .channelAccessToken(request.channelAccessToken())
                .channelSecret(request.channelSecret())
                .colorCode(request.colorCode())
                .build();

        return toResponse(lineChannelRepository.save(channel));
    }

    // แก้ไข LINE OA (เช่น เปลี่ยน token หรือสี)
    @Transactional
    public LineChannelResponse update(UUID id, LineChannelRequest request) {
        LineChannel channel = findById(id);

        // ป้องกันชื่อซ้ำกับ OA อื่น (ยกเว้นตัวเอง)
        lineChannelRepository.findByChannelName(request.channelName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT, "มี LINE OA ชื่อ \"" + request.channelName() + "\" อยู่แล้ว");
                    }
                });

        channel.setChannelName(request.channelName());
        channel.setColorCode(request.colorCode());

        // อัปเดต token เฉพาะเมื่อส่งมาใหม่ (ป้องกันลบ token โดยไม่ตั้งใจ)
        if (request.channelAccessToken() != null && !request.channelAccessToken().isBlank()) {
            channel.setChannelAccessToken(request.channelAccessToken());
        }
        if (request.channelSecret() != null && !request.channelSecret().isBlank()) {
            channel.setChannelSecret(request.channelSecret());
        }

        return toResponse(lineChannelRepository.save(channel));
    }

    // ลบ LINE OA
    @Transactional
    public void delete(UUID id) {
        findById(id); // ตรวจสอบว่ามีอยู่ก่อนลบ
        lineChannelRepository.deleteById(id);
    }

    // ดึง Webhook URL สำหรับนำไปตั้งใน LINE Developer Console
    @Transactional(readOnly = true)
    public String getWebhookUrl(UUID id, String baseUrl) {
        findById(id); // ตรวจสอบว่ามีอยู่
        return baseUrl + "/api/v1/webhook/" + id;
    }

    // ---------- helpers ----------

    private LineChannel findById(UUID id) {
        return lineChannelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ไม่พบ LINE OA ID: " + id));
    }

    // แปลง Entity → Response โดยไม่เปิดเผย token
    private LineChannelResponse toResponse(LineChannel c) {
        return new LineChannelResponse(
                c.getId(),
                c.getChannelName(),
                c.getColorCode(),
                c.getChannelAccessToken() != null && !c.getChannelAccessToken().isBlank(),
                c.getChannelSecret() != null && !c.getChannelSecret().isBlank()
        );
    }
}