package iot.platform.device.web.dto;

import iot.platform.device.model.Device;
import iot.platform.twin.model.DigitalTwin;

public final class DeviceMapper {

    private DeviceMapper() {
    }

    public static DeviceResponse toResponse(Device device) {
        DigitalTwin twin = device.getTwin();
        return DeviceResponse.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .twinId(twin == null ? null : twin.getId())
                .twinName(twin == null ? null : twin.getName())
                .twinType(twin == null ? null : twin.getType())
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
