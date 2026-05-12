package iot.platform.measurement.service;

import iot.platform.device.model.Device;
import iot.platform.device.service.DeviceService;
import iot.platform.event.MeasurementIngestedEvent;
import iot.platform.measurement.model.Measurement;
import iot.platform.measurement.repository.MeasurementRepository;
import iot.platform.measurement.web.dto.MeasurementCreateRequest;
import iot.platform.measurement.web.dto.MeasurementMapper;
import iot.platform.measurement.web.dto.MeasurementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final DeviceService deviceService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<MeasurementResponse> listForDevice(UUID deviceId, Pageable pageable) {
        Device device = deviceService.loadOwned(deviceId);
        return measurementRepository.findByDevice(device, pageable).map(MeasurementMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<MeasurementResponse> listRange(UUID deviceId, Instant from, Instant to) {
        Device device = deviceService.loadOwned(deviceId);
        return measurementRepository.findRange(device, from, to).stream()
                .map(MeasurementMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MeasurementResponse latestFor(UUID deviceId) {
        Device device = deviceService.loadOwned(deviceId);
        return measurementRepository.findFirstByDeviceOrderByTakenAtDesc(device)
                .map(MeasurementMapper::toResponse)
                .orElse(null);
    }

    @Transactional
    public MeasurementResponse recordSingle(UUID deviceId, MeasurementCreateRequest request) {
        Device device = deviceService.loadOwned(deviceId);
        Measurement saved = persist(device, request);
        log.info("Manual measurement recorded device={} takenAt={}", device.getId(), saved.getTakenAt());
        return MeasurementMapper.toResponse(saved);
    }

    @Transactional
    public Measurement persist(Device device, MeasurementCreateRequest request) {
        Measurement entity = Measurement.builder()
                .device(device)
                .takenAt(request.takenAt())
                .temperatureC(request.temperatureC())
                .humidityPct(request.humidityPct())
                .rawPayload(request.rawPayload())
                .build();
        Measurement saved = measurementRepository.save(entity);
        eventPublisher.publishEvent(new MeasurementIngestedEvent(device.getId(), saved.getId(), saved));
        return saved;
    }
}
