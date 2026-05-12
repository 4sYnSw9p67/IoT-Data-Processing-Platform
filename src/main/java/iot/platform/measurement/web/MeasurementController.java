package iot.platform.measurement.web;

import iot.platform.measurement.service.MeasurementService;
import iot.platform.measurement.web.dto.MeasurementCreateRequest;
import iot.platform.measurement.web.dto.MeasurementResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices/{deviceId}/measurements")
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;

    @GetMapping
    public ResponseEntity<Page<MeasurementResponse>> list(
            @PathVariable UUID deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(measurementService.listForDevice(deviceId,
                PageRequest.of(page, Math.min(size, 1000), Sort.by(Sort.Direction.DESC, "takenAt"))));
    }

    @GetMapping("/range")
    public ResponseEntity<List<MeasurementResponse>> range(
            @PathVariable UUID deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ResponseEntity.ok(measurementService.listRange(deviceId, from, to));
    }

    @GetMapping("/latest")
    public ResponseEntity<MeasurementResponse> latest(@PathVariable UUID deviceId) {
        MeasurementResponse latest = measurementService.latestFor(deviceId);
        return latest == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(latest);
    }

    @PostMapping
    public ResponseEntity<MeasurementResponse> record(
            @PathVariable UUID deviceId,
            @Valid @RequestBody MeasurementCreateRequest request) {
        return ResponseEntity.ok(measurementService.recordSingle(deviceId, request));
    }
}
