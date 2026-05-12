package iot.platform.forecast.web;

import iot.platform.forecast.client.dto.LocationForecastDto;
import iot.platform.forecast.client.dto.LocationResponseDto;
import iot.platform.forecast.service.ForecastService;
import iot.platform.forecast.web.dto.CreateLocationRequest;
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
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;

    @GetMapping("/locations")
    public ResponseEntity<List<LocationResponseDto>> list() {
        return ResponseEntity.ok(forecastService.listForCurrentUser());
    }

    @GetMapping("/locations/{id}")
    public ResponseEntity<LocationResponseDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(forecastService.get(id));
    }

    @PostMapping("/locations")
    public ResponseEntity<LocationResponseDto> create(@Valid @RequestBody CreateLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(forecastService.create(request));
    }

    @PutMapping("/locations/{id}/refresh")
    public ResponseEntity<LocationForecastDto> refresh(@PathVariable UUID id) {
        return ResponseEntity.ok(forecastService.refresh(id));
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        forecastService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/locations/{id}/forecast")
    public ResponseEntity<LocationForecastDto> forecast(@PathVariable UUID id) {
        return ResponseEntity.ok(forecastService.getForecast(id));
    }
}
