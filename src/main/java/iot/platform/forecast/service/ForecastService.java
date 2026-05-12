package iot.platform.forecast.service;

import iot.platform.exception.NotFoundException;
import iot.platform.forecast.client.ForecastFeignClient;
import iot.platform.forecast.client.dto.LocationForecastDto;
import iot.platform.forecast.client.dto.LocationRequestDto;
import iot.platform.forecast.client.dto.LocationResponseDto;
import iot.platform.forecast.web.dto.CreateLocationRequest;
import iot.platform.security.OwnershipGuard;
import iot.platform.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForecastService {

    static final String CACHE_NAME = "forecast";

    private final ForecastFeignClient feignClient;
    private final OwnershipGuard ownershipGuard;

    public List<LocationResponseDto> listForCurrentUser() {
        return feignClient.list(SecurityUtils.currentUserId());
    }

    public LocationResponseDto create(CreateLocationRequest request) {
        UUID currentUserId = SecurityUtils.currentUserId();
        LocationRequestDto body = new LocationRequestDto(
                currentUserId, request.label(), request.country(), request.latitude(), request.longitude());
        LocationResponseDto created = feignClient.create(body);
        log.info("Forecast location created via microservice id={} owner={}", created.id(), currentUserId);
        return created;
    }

    @CacheEvict(value = CACHE_NAME, key = "#id")
    public LocationForecastDto refresh(UUID id) {
        LocationResponseDto stored = loadOwned(id);
        log.info("Triggering forecast refresh location={} owner={}", id, stored.ownerUserId());
        return feignClient.refresh(id);
    }

    @CacheEvict(value = CACHE_NAME, key = "#id")
    public void delete(UUID id) {
        loadOwned(id);
        feignClient.delete(id);
        log.info("Forecast location deleted via microservice id={}", id);
    }

    @Cacheable(value = CACHE_NAME, key = "#id")
    public LocationForecastDto getForecast(UUID id) {
        loadOwned(id);
        log.debug("Fetching forecast from microservice (cache miss) id={}", id);
        return feignClient.forecast(id);
    }

    public LocationResponseDto get(UUID id) {
        return loadOwned(id);
    }

    private LocationResponseDto loadOwned(UUID id) {
        LocationResponseDto location;
        try {
            location = feignClient.get(id);
        } catch (RuntimeException ex) {
            throw new NotFoundException("Forecast location not found: " + id);
        }
        ownershipGuard.checkOwnership(location.ownerUserId());
        return location;
    }
}
