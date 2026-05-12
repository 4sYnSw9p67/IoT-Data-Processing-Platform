package iot.platform.measurement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IngestionAlertFlowApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void apiKeyIngestionTriggersThresholdAlert() throws Exception {
        String token = registerAndGetToken("iot-user", "iot@example.com", "Sup3rSecret!");
        String deviceId = createDevice(token, "Sensor-Hot",
                """
                {
                  "name": "Sensor-Hot",
                  "type": "COMBO",
                  "minTemperatureC": 10.0,
                  "maxTemperatureC": 25.0
                }
                """);

        MvcResult apiKeyResult = mockMvc.perform(post("/api/api-keys")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"raspi\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String apiKey = objectMapper.readTree(apiKeyResult.getResponse().getContentAsByteArray()).get("secret").asText();
        assertThat(apiKey).startsWith("iot_");

        String hotPayload = """
                {
                  "points": [
                    { "deviceId": "%s", "takenAt": "2026-05-13T12:00:00Z", "temperatureC": 30.0, "humidityPct": 55.0 }
                  ]
                }
                """.formatted(deviceId);

        mockMvc.perform(post("/api/ingest/measurements")
                        .header("X-Api-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(hotPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(1));

        mockMvc.perform(post("/api/ingest/measurements")
                        .header("X-Api-Key", "iot_obviously-invalid-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(hotPayload))
                .andExpect(status().isUnauthorized());

        MvcResult alertsResult = mockMvc.perform(get("/api/alerts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode alerts = objectMapper.readTree(alertsResult.getResponse().getContentAsByteArray());
        assertThat(alerts.isArray()).isTrue();
        assertThat(alerts.size()).isGreaterThanOrEqualTo(1);
        String alertId = alerts.get(0).get("id").asText();
        assertThat(alerts.get(0).get("source").asText()).isEqualTo("THRESHOLD");

        mockMvc.perform(put("/api/alerts/" + alertId + "/ack")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acknowledgedAt").isNotEmpty());
    }

    @Test
    void automationRuleProducesAlertOnRuleMatch() throws Exception {
        String token = registerAndGetToken("rule-user", "rule@example.com", "Sup3rSecret!");
        String deviceId = createDevice(token, "Sensor-Rule",
                """
                { "name": "Sensor-Rule", "type": "COMBO" }
                """);

        String ruleBody = """
                {
                  "name": "Humid bedroom",
                  "deviceId": "%s",
                  "conditionType": "HUMIDITY_ABOVE",
                  "threshold": 60.0,
                  "severity": "WARNING"
                }
                """.formatted(deviceId);
        mockMvc.perform(post("/api/rules")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ruleBody))
                .andExpect(status().isCreated());

        String measurementBody = """
                { "takenAt": "2026-05-13T13:00:00Z", "temperatureC": 22.0, "humidityPct": 75.0 }
                """;
        mockMvc.perform(post("/api/devices/" + deviceId + "/measurements")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(measurementBody))
                .andExpect(status().isOk());

        MvcResult alertsResult = mockMvc.perform(get("/api/alerts?unacknowledgedOnly=true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode alerts = objectMapper.readTree(alertsResult.getResponse().getContentAsByteArray());
        assertThat(alerts.size()).isEqualTo(1);
        assertThat(alerts.get(0).get("source").asText()).isEqualTo("RULE");
        assertThat(alerts.get(0).get("severity").asText()).isEqualTo("WARNING");
    }

    @Test
    void csvBulkUploadAcceptsValidRowsAndRejectsInvalidOnes() throws Exception {
        String token = registerAndGetToken("csv-user", "csv@example.com", "Sup3rSecret!");
        String deviceId = createDevice(token, "Sensor-Csv",
                """
                { "name": "Sensor-Csv", "type": "COMBO" }
                """);

        String csv = "deviceId,takenAt,temperatureC,humidityPct\n"
                + deviceId + ",2026-05-13T10:00:00Z,21.0,55.0\n"
                + deviceId + ",2026-05-13T10:20:00Z,21.4,57.0\n"
                + deviceId + ",2026-05-13T10:40:00Z,,58.0\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/measurements/bulk")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(3));

        String badCsv = "deviceId,takenAt,temperatureC\nnot-a-uuid,xx,21.0\n";
        MockMultipartFile badFile = new MockMultipartFile(
                "file", "bad.csv", "text/csv", badCsv.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/measurements/bulk")
                        .file(badFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    private String createDevice(String token, String name, String body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/devices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).get("id").asText();
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
}
