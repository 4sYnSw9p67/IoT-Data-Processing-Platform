package iot.platform.security.service;

import iot.platform.security.config.SecurityProperties;
import iot.platform.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final SecurityProperties securityProperties;

    public AccessToken issueAccessToken(User user) {
        Instant now = Instant.now();
        Duration ttl = Duration.ofMinutes(securityProperties.getJwt().getAccessTokenTtlMinutes());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(securityProperties.getJwt().getIssuer())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(() -> "HS256").build();
        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessToken(tokenValue, ttl.toSeconds());
    }

    public record AccessToken(String value, long expiresInSeconds) {
    }
}
