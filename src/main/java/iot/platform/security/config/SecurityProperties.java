package iot.platform.security.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.security")
@Validated
@Getter
@Setter
public class SecurityProperties {

    private final Jwt jwt = new Jwt();
    private final ApiKey apiKey = new ApiKey();

    @Getter
    @Setter
    public static class Jwt {

        @NotBlank
        private String issuer = "iot-platform";

        @Min(1)
        private long accessTokenTtlMinutes = 30;

        @Min(1)
        private long refreshTokenTtlDays = 14;
    }

    @Getter
    @Setter
    public static class ApiKey {

        @NotBlank
        private String headerName = "X-Api-Key";
    }
}
