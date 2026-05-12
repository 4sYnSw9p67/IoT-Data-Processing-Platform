package iot.platform.security;

import iot.platform.exception.InvalidTokenException;
import iot.platform.user.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID currentUserId() {
        return currentJwt()
                .map(Jwt::getSubject)
                .map(UUID::fromString)
                .orElseThrow(() -> new InvalidTokenException("No authenticated principal"));
    }

    public static Optional<UUID> currentUserIdOptional() {
        return currentJwt().map(Jwt::getSubject).map(UUID::fromString);
    }

    public static Optional<Jwt> currentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    public static boolean hasRole(Role role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_" + role.name()));
    }
}
