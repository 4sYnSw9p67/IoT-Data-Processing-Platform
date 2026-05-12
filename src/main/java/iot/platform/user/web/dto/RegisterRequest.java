package iot.platform.user.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Size(min = 3, max = 64)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username may contain letters, digits, dot, underscore, and hyphen")
        String username,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
        String password,

        @Size(max = 80)
        String firstName,

        @Size(max = 80)
        String lastName
) {
}
