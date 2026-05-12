package iot.platform.device.web.dto;

import iot.platform.device.model.Device;
import iot.platform.room.model.Room;

public final class DeviceMapper {

    private DeviceMapper() {
    }

    public static DeviceResponse toResponse(Device device) {
        Room room = device.getRoom();
        return DeviceResponse.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .roomId(room == null ? null : room.getId())
                .roomName(room == null ? null : room.getName())
                .ownerUserId(device.getOwnerUserId())
                .minTemperatureC(device.getMinTemperatureC())
                .maxTemperatureC(device.getMaxTemperatureC())
                .minHumidityPct(device.getMinHumidityPct())
                .maxHumidityPct(device.getMaxHumidityPct())
                .active(device.isActive())
                .registeredAt(device.getRegisteredAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}
