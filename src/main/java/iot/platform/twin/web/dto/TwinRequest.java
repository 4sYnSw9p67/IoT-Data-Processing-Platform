package iot.platform.twin.web.dto;

import iot.platform.twin.model.TwinType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TwinRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @NotNull
        TwinType type,

        UUID parentId,

        @Size(max = 500)
        String description,

        @Size(max = 40)
        String floor,

        @Pattern(regexp = "^#?[0-9a-fA-F]{6}$", message = "Color must be a 6-digit hex code")
        @Size(max = 16)
        String color,

        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double latitude,

        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double longitude
) {
}
