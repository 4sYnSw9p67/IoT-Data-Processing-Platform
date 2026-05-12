package iot.platform.security.service;

import iot.platform.exception.InvalidCredentialsException;
import iot.platform.security.model.RefreshToken;
import iot.platform.security.service.JwtTokenService.AccessToken;
import iot.platform.security.service.RefreshTokenService.IssuedRefreshToken;
import iot.platform.user.model.Role;
import iot.platform.user.model.User;
import iot.platform.user.service.UserService;
import iot.platform.user.web.dto.LoginRequest;
import iot.platform.user.web.dto.RegisterRequest;
import iot.platform.user.web.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        User user = userService.register(request, Role.USER);
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user;
        try {
            user = userService.getByUsername(request.username());
        } catch (RuntimeException ex) {
            throw new InvalidCredentialsException();
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Failed login attempt for username={}", request.username());
            throw new BadCredentialsException("Invalid credentials");
        }
        log.info("User logged in: id={}", user.getId());
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        RefreshToken rotated = refreshTokenService.rotate(refreshToken);
        User user = userService.getById(rotated.getUserId());
        log.info("Refresh token rotated for user={}", user.getId());
        return issueTokens(user);
    }

    @Transactional
    public void logout(java.util.UUID userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    private TokenResponse issueTokens(User user) {
        AccessToken access = jwtTokenService.issueAccessToken(user);
        IssuedRefreshToken refresh = refreshTokenService.issue(user.getId());
        return TokenResponse.builder()
                .accessToken(access.value())
                .refreshToken(refresh.value())
                .tokenType("Bearer")
                .expiresInSeconds(access.expiresInSeconds())
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
