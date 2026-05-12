package iot.platform.user.web.dto;

import iot.platform.user.model.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static ProfileResponse toProfile(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
