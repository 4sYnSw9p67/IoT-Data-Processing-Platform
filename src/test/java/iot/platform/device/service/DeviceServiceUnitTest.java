package iot.platform.device.service;

import iot.platform.device.model.Device;
import iot.platform.device.model.DeviceType;
import iot.platform.device.repository.DeviceRepository;
import iot.platform.device.web.dto.DeviceCreateRequest;
import iot.platform.device.web.dto.DeviceResponse;
import iot.platform.exception.ConflictException;
import iot.platform.measurement.repository.MeasurementRepository;
import iot.platform.room.service.RoomService;
import iot.platform.security.OwnershipGuard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceUnitTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private MeasurementRepository measurementRepository;

    @Mock
    private RoomService roomService;

    @Mock
    private OwnershipGuard ownershipGuard;

    @InjectMocks
    private DeviceService deviceService;

    @Test
    void createPersistsDeviceWithDefaults() {
        UUID owner = UUID.randomUUID();
        when(deviceRepository.existsByOwnerUserIdAndName(owner, "Sensor1")).thenReturn(false);
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
            Device d = invocation.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        DeviceCreateRequest request = new DeviceCreateRequest(
                "Sensor1", DeviceType.TEMPERATURE, null,
                10.0, 30.0, null, null, null);
        DeviceResponse response = deviceService.create(owner, request);

        assertThat(response.name()).isEqualTo("Sensor1");
        assertThat(response.type()).isEqualTo(DeviceType.TEMPERATURE);
        assertThat(response.active()).isTrue();
        assertThat(response.ownerUserId()).isEqualTo(owner);
    }

    @Test
    void createRejectsDuplicateName() {
        UUID owner = UUID.randomUUID();
        when(deviceRepository.existsByOwnerUserIdAndName(owner, "Sensor1")).thenReturn(true);
        DeviceCreateRequest request = new DeviceCreateRequest(
                "Sensor1", DeviceType.TEMPERATURE, null, null, null, null, null, null);
        assertThatThrownBy(() -> deviceService.create(owner, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Sensor1");
    }

    @Test
    void createRejectsInvertedThresholds() {
        UUID owner = UUID.randomUUID();
        when(deviceRepository.existsByOwnerUserIdAndName(owner, "BadSensor")).thenReturn(false);
        DeviceCreateRequest request = new DeviceCreateRequest(
                "BadSensor", DeviceType.COMBO, null,
                30.0, 10.0, null, null, null);
        assertThatThrownBy(() -> deviceService.create(owner, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("min temperature");
    }
}
