package com.example.ddchathub.service;

import com.example.ddchathub.dto.TagRequest;
import com.example.ddchathub.dto.TagResponse;
import com.example.ddchathub.entity.Tag;
import com.example.ddchathub.repository.TagRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName(), tag.getColorCode()))
                .toList();
    }

    @Transactional
    public TagResponse createTag(TagRequest request) {
        if (tagRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ชื่อแท็กนี้มีอยู่ในระบบแล้ว");
        }

        Tag tag = Tag.builder()
                .name(request.name())
                .colorCode(request.colorCode())
                .build();

        Tag savedTag = tagRepository.save(tag);

        return new TagResponse(savedTag.getId(), savedTag.getName(), savedTag.getColorCode());
    }

    @Transactional
    public TagResponse updateTag(UUID id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบแท็กที่ต้องการแก้ไข"));

        if (!tag.getName().equals(request.name()) && tagRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ชื่อแท็กนี้มีอยู่ในระบบแล้ว");
        }

        tag.setName(request.name());
        tag.setColorCode(request.colorCode());
        Tag updatedTag = tagRepository.save(tag);

        return new TagResponse(updatedTag.getId(), updatedTag.getName(), updatedTag.getColorCode());
    }

    @Transactional
    public void deleteTag(UUID id) {
        if (!tagRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบแท็กที่ต้องการลบ");
        }
        tagRepository.deleteById(id);
    }
}