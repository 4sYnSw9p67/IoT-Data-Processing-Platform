package iot.platform.room.service;

import iot.platform.aspect.Auditable;
import iot.platform.device.repository.DeviceRepository;
import iot.platform.exception.ConflictException;
import iot.platform.exception.NotFoundException;
import iot.platform.room.model.Room;
import iot.platform.room.repository.RoomRepository;
import iot.platform.room.web.dto.RoomMapper;
import iot.platform.room.web.dto.RoomRequest;
import iot.platform.room.web.dto.RoomResponse;
import iot.platform.security.OwnershipGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final OwnershipGuard ownershipGuard;

    @Transactional(readOnly = true)
    public List<RoomResponse> listForUser(UUID ownerUserId) {
        return roomRepository.findAllByOwnerUserIdOrderByNameAsc(ownerUserId).stream()
                .map(room -> RoomMapper.toResponse(room, deviceRepository.countByRoom(room)))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getById(UUID id) {
        Room room = loadOwned(id);
        return RoomMapper.toResponse(room, deviceRepository.countByRoom(room));
    }

    @Auditable("room.create")
    @Transactional
    public RoomResponse create(UUID ownerUserId, RoomRequest request) {
        if (roomRepository.existsByOwnerUserIdAndName(ownerUserId, request.name())) {
            throw new ConflictException("You already have a room called '" + request.name() + "'");
        }
        Room room = Room.builder()
                .name(request.name())
                .floor(request.floor())
                .color(normalizeColor(request.color()))
                .ownerUserId(ownerUserId)
                .build();
        Room saved = roomRepository.save(room);
        log.info("Room created id={} name={} owner={}", saved.getId(), saved.getName(), ownerUserId);
        return RoomMapper.toResponse(saved, 0L);
    }

    @Auditable("room.update")
    @Transactional
    public RoomResponse update(UUID id, RoomRequest request) {
        Room room = loadOwned(id);
        UUID ownerUserId = room.getOwnerUserId();
        if (!room.getName().equals(request.name()) && roomRepository.existsByOwnerUserIdAndName(ownerUserId, request.name())) {
            throw new ConflictException("You already have a room called '" + request.name() + "'");
        }
        room.setName(request.name());
        room.setFloor(request.floor());
        room.setColor(normalizeColor(request.color()));
        log.info("Room updated id={} owner={}", room.getId(), ownerUserId);
        return RoomMapper.toResponse(room, deviceRepository.countByRoom(room));
    }

    @Auditable("room.delete")
    @Transactional
    public void delete(UUID id) {
        Room room = loadOwned(id);
        long deviceCount = deviceRepository.countByRoom(room);
        if (deviceCount > 0) {
            throw new ConflictException("Cannot delete room with " + deviceCount + " assigned device(s)");
        }
        roomRepository.delete(room);
        log.info("Room deleted id={} owner={}", id, room.getOwnerUserId());
    }

    @Transactional(readOnly = true)
    public Room loadOwned(UUID id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found: " + id));
        ownershipGuard.checkOwnership(room.getOwnerUserId());
        return room;
    }

    private String normalizeColor(String color) {
        if (!StringUtils.hasText(color)) {
            return null;
        }
        return color.startsWith("#") ? color.toLowerCase() : "#" + color.toLowerCase();
    }
}
