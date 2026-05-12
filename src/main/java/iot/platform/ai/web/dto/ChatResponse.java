package iot.platform.ai.web.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record ChatResponse(
        String reply,
        Instant respondedAt,
        List<UUID> devicesConsidered,
        int measurementsConsidered
) {
}
