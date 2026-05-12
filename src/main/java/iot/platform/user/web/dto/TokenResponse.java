package iot.platform.user.web.dto;

import iot.platform.user.model.Role;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UUID userId,
        String username,
        Role role
) {
}
