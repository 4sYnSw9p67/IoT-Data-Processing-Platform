package iot.platform.device;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeviceFlowApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullDeviceLifecycleWorks() throws Exception {
        String token = registerAndGetToken("zoe", "zoe@example.com", "ZoePass!123");

        String roomBody = """
                { "name": "Living Room", "floor": "1", "color": "#aabbcc" }
                """;
        MvcResult roomResult = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Living Room"))
                .andReturn();
        String roomId = objectMapper.readTree(roomResult.getResponse().getContentAsByteArray()).get("id").asText();

        String deviceBody = """
                {
                  "name": "TempSensor-LR",
                  "type": "COMBO",
                  "roomId": "%s",
                  "minTemperatureC": 16.0,
                  "maxTemperatureC": 27.0,
                  "minHumidityPct": 30.0,
                  "maxHumidityPct": 70.0
                }
                """.formatted(roomId);
        MvcResult deviceResult = mockMvc.perform(post("/api/devices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deviceBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(roomId))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();
        String deviceId = objectMapper.readTree(deviceResult.getResponse().getContentAsByteArray()).get("id").asText();

        String updateBody = """
                { "minTemperatureC": 18.0, "maxTemperatureC": 25.0, "active": false }
                """;
        mockMvc.perform(put("/api/devices/" + deviceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minTemperatureC").value(18.0))
                .andExpect(jsonPath("$.active").value(false));

        String invalidUpdate = """
                { "minTemperatureC": 30.0, "maxTemperatureC": 20.0 }
                """;
        mockMvc.perform(put("/api/devices/" + deviceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUpdate))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/devices/" + deviceId + "/room")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\": null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").doesNotExist());

        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceCount").value(0));

        mockMvc.perform(delete("/api/rooms/" + roomId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/devices/" + deviceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/devices/" + deviceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotAccessSomeoneElsesDevice() throws Exception {
        String tokenA = registerAndGetToken("ownerA", "ownerA@example.com", "OwnerPass1!");
        String tokenB = registerAndGetToken("ownerB", "ownerB@example.com", "OwnerPass2!");

        String deviceBody = """
                { "name": "Hidden", "type": "TEMPERATURE", "minTemperatureC": 10.0, "maxTemperatureC": 30.0 }
                """;
        MvcResult result = mockMvc.perform(post("/api/devices")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deviceBody))
                .andExpect(status().isCreated())
                .andReturn();
        String deviceId = objectMapper.readTree(result.getResponse().getContentAsByteArray()).get("id").asText();

        mockMvc.perform(get("/api/devices/" + deviceId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/devices/" + deviceId)
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
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return json.get("accessToken").asText();
    }
}
