package iot.platform.user.web.dto;

import iot.platform.user.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(@NotNull Role role) {
}
