package iot.platform.device.web;

import iot.platform.device.service.DeviceService;
import iot.platform.device.web.dto.AssignRoomRequest;
import iot.platform.device.web.dto.DeviceCreateRequest;
import iot.platform.device.web.dto.DeviceResponse;
import iot.platform.device.web.dto.DeviceUpdateRequest;
import iot.platform.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> list() {
        return ResponseEntity.ok(deviceService.listForUser(SecurityUtils.currentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceCreateRequest request) {
        DeviceResponse created = deviceService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> update(@PathVariable UUID id, @Valid @RequestBody DeviceUpdateRequest request) {
        return ResponseEntity.ok(deviceService.update(id, request));
    }

    @PutMapping("/{id}/room")
    public ResponseEntity<DeviceResponse> assignRoom(@PathVariable UUID id, @RequestBody AssignRoomRequest request) {
        return ResponseEntity.ok(deviceService.assignRoom(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
