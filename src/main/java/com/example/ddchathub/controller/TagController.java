package com.example.ddchathub.controller;

import com.example.ddchathub.dto.TagRequest;
import com.example.ddchathub.dto.TagResponse;
import com.example.ddchathub.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 💡 อนุญาตให้ React (หรือทุกโดเมน) ยิง API เข้ามาได้
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagRequest request) {
        return new ResponseEntity<>(tagService.createTag(request), HttpStatus.CREATED);
    }

    // 💡 ฟีเจอร์ใหม่: สำหรับแก้ไขชื่อหรือสีของแท็ก
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable UUID id,
            @Valid @RequestBody TagRequest request) {
        // อัปเดตเสร็จแล้วส่ง 200 OK กลับไปพร้อมข้อมูลใหม่
        return ResponseEntity.ok(tagService.updateTag(id, request));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}