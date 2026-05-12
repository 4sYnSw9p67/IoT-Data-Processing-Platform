package iot.platform.measurement.service;

import iot.platform.aspect.Auditable;
import iot.platform.device.model.Device;
import iot.platform.device.repository.DeviceRepository;
import iot.platform.exception.ConflictException;
import iot.platform.exception.NotFoundException;
import iot.platform.measurement.web.dto.IngestBatchRequest;
import iot.platform.measurement.web.dto.IngestPoint;
import iot.platform.measurement.web.dto.IngestResult;
import iot.platform.measurement.web.dto.MeasurementCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {

    private final DeviceRepository deviceRepository;
    private final MeasurementService measurementService;

    @Transactional
    public IngestResult ingestBatch(UUID ownerUserId, IngestBatchRequest request) {
        Map<UUID, Device> deviceCache = new HashMap<>();
        int accepted = 0;
        List<IngestResult.Failure> failures = new ArrayList<>();
        for (int i = 0; i < request.points().size(); i++) {
            IngestPoint point = request.points().get(i);
            try {
                Device device = loadOwnedDevice(point.deviceId(), ownerUserId, deviceCache);
                MeasurementCreateRequest req = new MeasurementCreateRequest(
                        point.takenAt(), point.temperatureC(), point.humidityPct(), point.rawPayload());
                measurementService.persist(device, req);
                accepted++;
            } catch (RuntimeException ex) {
                failures.add(new IngestResult.Failure(i, ex.getMessage()));
                log.warn("Rejected measurement row {} owner={} reason={}", i, ownerUserId, ex.getMessage());
            }
        }
        log.info("Ingest batch complete owner={} accepted={} rejected={}", ownerUserId, accepted, failures.size());
        return new IngestResult(accepted, failures);
    }

    @Auditable("measurement.bulkUpload")
    @Transactional
    public IngestResult ingestCsv(UUID ownerUserId, MultipartFile file) {
        List<IngestPoint> points = parseCsv(file);
        return ingestBatch(ownerUserId, new IngestBatchRequest(points));
    }

    private List<IngestPoint> parseCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ConflictException("CSV file is required and must not be empty");
        }
        List<IngestPoint> points = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                throw new ConflictException("CSV file is empty");
            }
            String[] columns = header.trim().toLowerCase().split(",");
            int deviceIdx = indexOf(columns, "deviceid");
            int takenAtIdx = indexOf(columns, "takenat");
            int tempIdx = indexOfOptional(columns, "temperaturec");
            int humIdx = indexOfOptional(columns, "humiditypct");
            int lineNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                try {
                    UUID deviceId = UUID.fromString(parts[deviceIdx].trim());
                    Instant takenAt = Instant.parse(parts[takenAtIdx].trim());
                    Double temp = tempIdx == -1 ? null : parseDouble(parts[tempIdx]);
                    Double hum = humIdx == -1 ? null : parseDouble(parts[humIdx]);
                    points.add(new IngestPoint(deviceId, takenAt, temp, hum, null));
                } catch (IllegalArgumentException | DateTimeParseException | ArrayIndexOutOfBoundsException ex) {
                    throw new ConflictException("Invalid CSV row at line " + lineNumber + ": " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new ConflictException("Failed to read CSV: " + ex.getMessage());
        }
        if (points.isEmpty()) {
            throw new ConflictException("CSV contained no data rows");
        }
        return points;
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Double.parseDouble(value.trim());
    }

    private int indexOf(String[] columns, String name) {
        int idx = indexOfOptional(columns, name);
        if (idx == -1) {
            throw new ConflictException("CSV missing required column: " + name);
        }
        return idx;
    }

    private int indexOfOptional(String[] columns, String name) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private Device loadOwnedDevice(UUID deviceId, UUID ownerUserId, Map<UUID, Device> cache) {
        Device cached = cache.get(deviceId);
        if (cached != null) {
            return cached;
        }
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new NotFoundException("Device not found: " + deviceId));
        if (!device.getOwnerUserId().equals(ownerUserId)) {
            throw new AccessDeniedException("Device " + deviceId + " is not owned by ingestion principal");
        }
        cache.put(deviceId, device);
        return device;
    }
}
