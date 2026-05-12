package iot.platform.ai.service;

import iot.platform.ai.web.dto.ChatResponse;
import iot.platform.device.model.Device;
import iot.platform.device.repository.DeviceRepository;
import iot.platform.measurement.model.Measurement;
import iot.platform.measurement.repository.MeasurementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AiAssistantService {

    private static final String SYSTEM_PROMPT = """
            You are an assistant for the IoT Data Processing Platform. You help homeowners
            understand temperature and humidity trends from their sensors. Be concise, use
            metric units, reference the device names provided in the user data, and refuse
            to answer questions unrelated to the captured measurements.
            """;

    private final boolean enabled;
    private final int contextHours;
    private final Optional<ChatClient> chatClient;
    private final DeviceRepository deviceRepository;
    private final MeasurementRepository measurementRepository;

    public AiAssistantService(
            @Value("${app.ai.enabled:false}") boolean enabled,
            @Value("${app.ai.measurement-context-hours:24}") int contextHours,
            Optional<ChatClient.Builder> chatClientBuilder,
            DeviceRepository deviceRepository,
            MeasurementRepository measurementRepository) {
        this.enabled = enabled;
        this.contextHours = contextHours;
        this.chatClient = chatClientBuilder.map(ChatClient.Builder::build);
        this.deviceRepository = deviceRepository;
        this.measurementRepository = measurementRepository;
    }

    public boolean isEnabled() {
        return enabled && chatClient.isPresent();
    }

    @Transactional(readOnly = true)
    public ChatResponse ask(UUID ownerUserId, String question) {
        if (!isEnabled()) {
            throw new IllegalStateException("AI assistant is disabled. Set app.ai.enabled=true and provide an OpenAI API key.");
        }
        List<Device> devices = deviceRepository.findAllByOwnerUserIdOrderByNameAsc(ownerUserId);
        Instant from = Instant.now().minus(contextHours, ChronoUnit.HOURS);
        Instant to = Instant.now();
        List<UUID> deviceIds = new ArrayList<>();
        int measurementsCount = 0;
        StringBuilder context = new StringBuilder();
        for (Device device : devices) {
            List<Measurement> rows = measurementRepository.findRange(device, from, to);
            if (rows.isEmpty()) {
                continue;
            }
            deviceIds.add(device.getId());
            measurementsCount += rows.size();
            double avgTemp = rows.stream()
                    .filter(m -> m.getTemperatureC() != null)
                    .mapToDouble(Measurement::getTemperatureC)
                    .average().orElse(Double.NaN);
            double avgHum = rows.stream()
                    .filter(m -> m.getHumidityPct() != null)
                    .mapToDouble(Measurement::getHumidityPct)
                    .average().orElse(Double.NaN);
            context.append(String.format(
                    "- %s (%s) over the last %dh: %d readings, average %.1f°C, %.1f%% humidity%n",
                    device.getName(), device.getType(), contextHours, rows.size(), avgTemp, avgHum));
        }
        if (context.length() == 0) {
            context.append("No measurements were recorded in the past ").append(contextHours).append(" hours.");
        }
        String prompt = "Recent sensor context:\n" + context + "\n\nUser question: " + question;
        log.info("AI chat invoked owner={} devices={} measurements={}", ownerUserId, deviceIds.size(), measurementsCount);
        String reply = chatClient.get().prompt()
                .system(SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .content();
        return ChatResponse.builder()
                .reply(reply)
                .respondedAt(Instant.now())
                .devicesConsidered(deviceIds)
                .measurementsConsidered(measurementsCount)
                .build();
    }
}
