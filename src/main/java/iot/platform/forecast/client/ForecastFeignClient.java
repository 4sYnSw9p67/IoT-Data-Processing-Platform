package iot.platform.forecast.client;

import iot.platform.forecast.client.dto.LocationForecastDto;
import iot.platform.forecast.client.dto.LocationRequestDto;
import iot.platform.forecast.client.dto.LocationResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "weather-forecast", url = "${app.forecast-client.base-url}", path = "/api/locations")
public interface ForecastFeignClient {

    @GetMapping
    List<LocationResponseDto> list(@RequestParam UUID ownerUserId);

    @GetMapping("/{id}")
    LocationResponseDto get(@PathVariable UUID id);

    @PostMapping
    LocationResponseDto create(@RequestBody LocationRequestDto request);

    @PutMapping("/{id}/refresh")
    LocationForecastDto refresh(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id);

    @GetMapping("/{id}/forecast")
    LocationForecastDto forecast(@PathVariable UUID id);
}
