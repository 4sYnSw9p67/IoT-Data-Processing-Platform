package iot.platform.security;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginRefreshProfileFlowSucceeds() throws Exception {
        String registerBody = """
                {
                  "username": "alice",
                  "email": "alice@example.com",
                  "password": "Sup3rSecret!",
                  "firstName": "Alice",
                  "lastName": "Doe"
                }
                """;

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        JsonNode registerJson = objectMapper.readTree(registerResult.getResponse().getContentAsByteArray());
        String accessToken = registerJson.get("accessToken").asText();
        String refreshToken = registerJson.get("refreshToken").asText();

        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        String loginBody = """
                { "username": "alice", "password": "Sup3rSecret!" }
                """;
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        String refreshBody = "{\"refreshToken\":\"" + refreshToken + "\"}";
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode refreshJson = objectMapper.readTree(refreshResult.getResponse().getContentAsByteArray());
        assertThat(refreshJson.get("refreshToken").asText()).isNotEqualTo(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isUnauthorized());

        String updateBody = """
                { "firstName": "Alicia", "lastName": "Doe", "email": "alice@example.com" }
                """;
        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alicia"));
    }

    @Test
    void adminEndpointForbiddenForRegularUser() throws Exception {
        String registerBody = """
                {
                  "username": "bob",
                  "email": "bob@example.com",
                  "password": "Pa55word!aa"
                }
                """;
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn();
        String token = objectMapper.readTree(result.getResponse().getContentAsByteArray()).get("accessToken").asText();

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidLoginReturnsUnauthorized() throws Exception {
        String body = """
                { "username": "nope", "password": "wrongpass" }
                """;
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerValidationFailsForShortPassword() throws Exception {
        String body = """
                { "username": "x", "email": "not-an-email", "password": "abc" }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray());
    }
}
