package iot.platform.twin.web.dto;

import iot.platform.twin.model.DigitalTwin;

public final class TwinMapper {

    private TwinMapper() {
    }

    public static TwinResponse toResponse(DigitalTwin twin, long deviceCount, long childCount) {
        DigitalTwin parent = twin.getParent();
        return TwinResponse.builder()
                .id(twin.getId())
                .name(twin.getName())
                .type(twin.getType())
                .parentId(parent == null ? null : parent.getId())
                .parentName(parent == null ? null : parent.getName())
                .ownerUserId(twin.getOwnerUserId())
                .description(twin.getDescription())
                .floor(twin.getFloor())
                .color(twin.getColor())
                .latitude(twin.getLatitude())
                .longitude(twin.getLongitude())
                .deviceCount(deviceCount)
                .childCount(childCount)
                .createdAt(twin.getCreatedAt())
                .updatedAt(twin.getUpdatedAt())
                .build();
    }
}
