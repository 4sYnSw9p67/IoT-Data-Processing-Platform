package iot.platform.forecast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iot.platform.forecast.client.ForecastFeignClient;
import iot.platform.forecast.client.dto.ForecastDayDto;
import iot.platform.forecast.client.dto.LocationForecastDto;
import iot.platform.forecast.client.dto.LocationRequestDto;
import iot.platform.forecast.client.dto.LocationResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ForecastFlowApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private ForecastFeignClient feignClient;

    @Test
    void createRefreshAndGetCachedForecast() throws Exception {
        String token = registerAndGetToken("forecast-user", "forecast@example.com", "Sup3rSecret!");

        UUID locationId = UUID.randomUUID();
        UUID ownerId = ownerIdFromToken(token);

        LocationResponseDto created = new LocationResponseDto(
                locationId, ownerId, "Sofia", "Bulgaria", 42.7, 23.3,
                "Europe/Sofia", null, Instant.now(), Instant.now());
        when(feignClient.create(any(LocationRequestDto.class))).thenReturn(created);
        when(feignClient.get(locationId)).thenReturn(created);

        LocationForecastDto forecast = new LocationForecastDto(
                locationId, "Sofia", "Europe/Sofia", Instant.now(),
                List.of(new ForecastDayDto(LocalDate.now(), 14.0, 25.0, 60.0, 0.0, 10.0)));
        when(feignClient.refresh(locationId)).thenReturn(forecast);
        when(feignClient.forecast(locationId)).thenReturn(forecast);

        String createBody = """
                {
                  "label": "Sofia",
                  "country": "Bulgaria",
                  "latitude": 42.7,
                  "longitude": 23.3
                }
                """;
        mockMvc.perform(post("/api/forecast/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(locationId.toString()));

        mockMvc.perform(put("/api/forecast/locations/" + locationId + "/refresh")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Sofia"));

        mockMvc.perform(get("/api/forecast/locations/" + locationId + "/forecast")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/forecast/locations/" + locationId + "/forecast")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(feignClient, times(1)).forecast(eq(locationId));
        assertThat(cacheManager.getCache("forecast")).isNotNull();
        assertThat(cacheManager.getCache("forecast").get(locationId)).isNotNull();
    }

    @Test
    void otherUserCannotAccessSomeoneElsesLocation() throws Exception {
        String tokenA = registerAndGetToken("fa-user", "fa@example.com", "Pa55Pa55!");
        String tokenB = registerAndGetToken("fb-user", "fb@example.com", "Pa55Pa55!");
        UUID locationId = UUID.randomUUID();
        UUID ownerA = ownerIdFromToken(tokenA);

        LocationResponseDto owned = new LocationResponseDto(
                locationId, ownerA, "Plovdiv", "Bulgaria", 42.1, 24.7,
                "Europe/Sofia", null, Instant.now(), Instant.now());
        when(feignClient.get(locationId)).thenReturn(owned);

        mockMvc.perform(get("/api/forecast/locations/" + locationId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    private String registerAndGetToken(String username, String email, String password) throws Exception {
        String body = """
                { "username": "%s", "email": "%s", "password": "%s" }
                """.formatted(username, email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).get("accessToken").asText();
    }

    private UUID ownerIdFromToken(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return UUID.fromString(json.get("id").asText());
    }
}
