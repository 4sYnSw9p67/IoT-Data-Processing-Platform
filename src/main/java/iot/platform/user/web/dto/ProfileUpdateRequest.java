package iot.platform.user.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(

        @Size(max = 80)
        String firstName,

        @Size(max = 80)
        String lastName,

        @Email
        @Size(max = 254)
        String email
) {
}
