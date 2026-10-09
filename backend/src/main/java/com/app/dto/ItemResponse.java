package com.app.dto;

import com.app.model.Item;

import java.time.LocalDateTime;

public record ItemResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(item.getId(), item.getName(), item.getDescription(),
                item.getCreatedAt(), item.getUpdatedAt());
    }
}
