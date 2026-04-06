package com.example.ddchathub.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String fullName,
        String profilePictureUrl,
        String realName,
        String phoneNumber,
        List<TagResponse> tags,
        String channelName,
        String channelColor,
        UUID channelId
) {}