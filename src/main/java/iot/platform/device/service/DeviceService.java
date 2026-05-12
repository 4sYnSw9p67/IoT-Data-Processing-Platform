package iot.platform.device.service;

import iot.platform.device.model.Device;
import iot.platform.device.repository.DeviceRepository;
import iot.platform.device.web.dto.AssignRoomRequest;
import iot.platform.device.web.dto.DeviceCreateRequest;
import iot.platform.device.web.dto.DeviceMapper;
import iot.platform.device.web.dto.DeviceResponse;
import iot.platform.device.web.dto.DeviceUpdateRequest;
import iot.platform.exception.ConflictException;
import iot.platform.exception.NotFoundException;
import iot.platform.measurement.repository.MeasurementRepository;
import iot.platform.room.model.Room;
import iot.platform.room.service.RoomService;
import iot.platform.security.OwnershipGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final MeasurementRepository measurementRepository;
    private final RoomService roomService;
    private final OwnershipGuard ownershipGuard;

    @Transactional(readOnly = true)
    public List<DeviceResponse> listForUser(UUID ownerUserId) {
        return deviceRepository.findAllByOwnerUserIdOrderByNameAsc(ownerUserId).stream()
                .map(DeviceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceResponse getById(UUID id) {
        return DeviceMapper.toResponse(loadOwned(id));
    }

    @Transactional
    public DeviceResponse create(UUID ownerUserId, DeviceCreateRequest request) {
        if (deviceRepository.existsByOwnerUserIdAndName(ownerUserId, request.name())) {
            throw new ConflictException("You already have a device named '" + request.name() + "'");
        }
        validateThresholds(request.minTemperatureC(), request.maxTemperatureC(),
                request.minHumidityPct(), request.maxHumidityPct());
        Room room = null;
        if (request.roomId() != null) {
            room = roomService.loadOwned(request.roomId());
        }
        Device device = Device.builder()
                .name(request.name())
                .type(request.type())
                .room(room)
                .ownerUserId(ownerUserId)
                .minTemperatureC(request.minTemperatureC())
                .maxTemperatureC(request.maxTemperatureC())
                .minHumidityPct(request.minHumidityPct())
                .maxHumidityPct(request.maxHumidityPct())
                .active(request.active() == null || request.active())
                .build();
        Device saved = deviceRepository.save(device);
        log.info("Device created id={} name={} type={} owner={}", saved.getId(), saved.getName(), saved.getType(), ownerUserId);
        return DeviceMapper.toResponse(saved);
    }

    @Transactional
    public DeviceResponse update(UUID id, DeviceUpdateRequest request) {
        Device device = loadOwned(id);
        if (request.name() != null && !request.name().equals(device.getName())) {
            if (deviceRepository.existsByOwnerUserIdAndName(device.getOwnerUserId(), request.name())) {
                throw new ConflictException("You already have a device named '" + request.name() + "'");
            }
            device.setName(request.name());
        }
        if (request.minTemperatureC() != null) {
            device.setMinTemperatureC(request.minTemperatureC());
        }
        if (request.maxTemperatureC() != null) {
            device.setMaxTemperatureC(request.maxTemperatureC());
        }
        if (request.minHumidityPct() != null) {
            device.setMinHumidityPct(request.minHumidityPct());
        }
        if (request.maxHumidityPct() != null) {
            device.setMaxHumidityPct(request.maxHumidityPct());
        }
        if (request.active() != null) {
            device.setActive(request.active());
        }
        validateThresholds(device.getMinTemperatureC(), device.getMaxTemperatureC(),
                device.getMinHumidityPct(), device.getMaxHumidityPct());
        log.info("Device updated id={} owner={}", device.getId(), device.getOwnerUserId());
        return DeviceMapper.toResponse(device);
    }

    @Transactional
    public DeviceResponse assignRoom(UUID id, AssignRoomRequest request) {
        Device device = loadOwned(id);
        if (request.roomId() == null) {
            device.setRoom(null);
        } else {
            Room room = roomService.loadOwned(request.roomId());
            device.setRoom(room);
        }
        log.info("Device id={} assigned to room {}", id, request.roomId());
        return DeviceMapper.toResponse(device);
    }

    @Transactional
    public void delete(UUID id) {
        Device device = loadOwned(id);
        int removed = measurementRepository.deleteByDevice(device);
        deviceRepository.delete(device);
        log.info("Device deleted id={} cascaded {} measurements", id, removed);
    }

    @Transactional(readOnly = true)
    public Device loadOwned(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Device not found: " + id));
        ownershipGuard.checkOwnership(device.getOwnerUserId());
        return device;
    }

    private void validateThresholds(Double minTemp, Double maxTemp, Double minHum, Double maxHum) {
        if (minTemp != null && maxTemp != null && minTemp > maxTemp) {
            throw new ConflictException("min temperature must not exceed max temperature");
        }
        if (minHum != null && maxHum != null && minHum > maxHum) {
            throw new ConflictException("min humidity must not exceed max humidity");
        }
    }
}
