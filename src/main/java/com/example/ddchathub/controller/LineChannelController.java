package com.example.ddchathub.controller;

import com.example.ddchathub.dto.LineChannelRequest;
import com.example.ddchathub.dto.LineChannelResponse;
import com.example.ddchathub.service.LineChannelService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LineChannelController {

    private final LineChannelService lineChannelService;

    // ดูรายการ LINE OA ทั้งหมด — ทุก admin เห็นได้
    @GetMapping
    public ResponseEntity<List<LineChannelResponse>> getAll() {
        return ResponseEntity.ok(lineChannelService.getAll());
    }

    // เพิ่ม LINE OA ใหม่ — เฉพาะ SUPER_ADMIN
    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')") // 💡 เปลี่ยนมาใช้อันนี้ชัวร์กว่าครับ!
    public ResponseEntity<LineChannelResponse> create(
            @Valid @RequestBody LineChannelRequest request) {
        return new ResponseEntity<>(lineChannelService.create(request), HttpStatus.CREATED);
    }

    // แก้ไข LINE OA — เฉพาะ SUPER_ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')") // 💡 เปลี่ยนมาใช้อันนี้ชัวร์กว่าครับ!
    public ResponseEntity<LineChannelResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody LineChannelRequest request) {
        return ResponseEntity.ok(lineChannelService.update(id, request));
    }

    // ลบ LINE OA — เฉพาะ SUPER_ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')") // 💡 เปลี่ยนมาใช้อันนี้ชัวร์กว่าครับ!
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lineChannelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ดู Webhook URL สำหรับนำไปตั้งใน LINE Developer Console
    @GetMapping("/{id}/webhook-url")
    public ResponseEntity<Map<String, String>> getWebhookUrl(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        String baseUrl = httpRequest.getScheme() + "://" + httpRequest.getServerName()
                + (httpRequest.getServerPort() == 80 || httpRequest.getServerPort() == 443
                ? "" : ":" + httpRequest.getServerPort());

        String webhookUrl = lineChannelService.getWebhookUrl(id, baseUrl);
        return ResponseEntity.ok(Map.of("webhookUrl", webhookUrl));
    }
}