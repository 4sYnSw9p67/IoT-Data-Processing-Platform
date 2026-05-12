package iot.platform.room.web.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record RoomResponse(
        UUID id,
        String name,
        String floor,
        String color,
        UUID ownerUserId,
        long deviceCount,
        Instant createdAt,
        Instant updatedAt
) {
}
