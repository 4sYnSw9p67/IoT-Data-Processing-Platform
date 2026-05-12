package iot.platform.security;

import iot.platform.user.model.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OwnershipGuard {

    public void checkOwnership(UUID resourceOwnerUserId) {
        UUID currentUser = SecurityUtils.currentUserId();
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }
        if (!currentUser.equals(resourceOwnerUserId)) {
            throw new AccessDeniedException("You do not own this resource");
        }
    }
}
