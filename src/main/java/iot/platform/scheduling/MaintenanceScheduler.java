package iot.platform.scheduling;

import iot.platform.measurement.repository.MeasurementRepository;
import iot.platform.security.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class MaintenanceScheduler {

    private static final String FORECAST_CACHE = "forecast";

    private final MeasurementRepository measurementRepository;
    private final RefreshTokenService refreshTokenService;
    private final CacheManager cacheManager;

    @Value("${app.scheduling.raw-measurement-retention-days:90}")
    private int rawMeasurementRetentionDays;

    @Scheduled(cron = "${app.scheduling.measurement-aggregation-cron:0 0 3 * * *}")
    public void dailyMaintenance() {
        Instant measurementCutoff = Instant.now().minus(rawMeasurementRetentionDays, ChronoUnit.DAYS);
        int prunedMeasurements = measurementRepository.deleteOlderThan(measurementCutoff);
        int prunedTokens = refreshTokenService.purgeExpired();
        log.info("Daily maintenance done: pruned {} measurements older than {} and {} expired refresh tokens",
                prunedMeasurements, measurementCutoff, prunedTokens);
    }

    @Scheduled(fixedDelayString = "${app.scheduling.forecast-refresh-fixed-delay-ms:900000}",
            initialDelayString = "${app.scheduling.forecast-refresh-fixed-delay-ms:900000}")
    public void evictForecastCache() {
        if (cacheManager.getCache(FORECAST_CACHE) != null) {
            cacheManager.getCache(FORECAST_CACHE).clear();
            log.debug("Forecast cache evicted by scheduler");
        }
    }
}
