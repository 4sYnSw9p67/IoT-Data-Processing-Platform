package iot.platform.user.web.dto;

import iot.platform.user.model.Role;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ProfileResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        Role role,
        boolean enabled,
        Instant createdAt
) {
}
