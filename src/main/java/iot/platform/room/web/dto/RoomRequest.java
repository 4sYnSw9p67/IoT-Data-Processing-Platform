package iot.platform.room.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoomRequest(

        @NotBlank
        @Size(max = 80)
        String name,

        @Size(max = 20)
        String floor,

        @Pattern(regexp = "^#?[0-9a-fA-F]{6}$", message = "Color must be a 6-digit hex code")
        @Size(max = 16)
        String color
) {
}
