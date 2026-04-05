package com.example.ddchathub.dto;


import java.util.UUID;

public record TagResponse(
        UUID id,
        String name,
        String colorCode
) {}
