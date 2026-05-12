package iot.platform.room.web.dto;

import iot.platform.room.model.Room;

public final class RoomMapper {

    private RoomMapper() {
    }

    public static RoomResponse toResponse(Room room, long deviceCount) {
        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .floor(room.getFloor())
                .color(room.getColor())
                .ownerUserId(room.getOwnerUserId())
                .deviceCount(deviceCount)
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
