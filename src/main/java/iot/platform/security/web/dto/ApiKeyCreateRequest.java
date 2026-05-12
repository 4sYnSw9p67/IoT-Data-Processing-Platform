package iot.platform.security.web.dto;

import jakarta.validation.constraints.Size;

public record ApiKeyCreateRequest(
        @Size(max = 80) String label
) {
}
