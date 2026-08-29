package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.match.dto.MatchDTO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MatchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Flujo E2E: Create, join, list in Dashboard and start a match")
    @WithMockUser(username = "admin1", authorities = {"ADMIN"})
    void testMatchLifecycle() throws Exception {
        // 1. Create a match
        Map<String, String> body = Map.of("name", "Integration Match", "password", "pass");

        MvcResult result = mockMvc.perform(post("/api/v1/matches/matchCreator/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Integration Match"))
            .andReturn();

        String content = result.getResponse().getContentAsString();
        MatchDTO createdMatch = objectMapper.readValue(content, MatchDTO.class);
        Integer matchId = createdMatch.getId();

        // 2. Join the match as a player
        mockMvc.perform(post("/api/v1/players/match/" + matchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"user\": \"1\", \"color\": \"RED\"}"))
            .andExpect(status().isCreated());

        // 3. Verify it appears on the dashboard
        mockMvc.perform(get("/api/v1/matches/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].name").exists());

        // 4. Start the match
        mockMvc.perform(patch("/api/v1/matches/" + matchId + "/startGame"))
            .andExpect(status().isOk());

        // 5. Get the match details to confirm state changed
        mockMvc.perform(get("/api/v1/matches/" + matchId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("ON_GOING"));
    }

    @Test
    @DisplayName("Negative Cases: Error Handling for Database and Routes")
    @WithMockUser(username = "admin1", authorities = {"ADMIN"})
    void testMatchNegativeCases() throws Exception {
        // 1. Get non-existent match
        mockMvc.perform(get("/api/v1/matches/999"))
            .andExpect(status().isNotFound());

        // 2. Create match with non-existent user
        mockMvc.perform(post("/api/v1/matches/matchCreator/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Fail Match\"}"))
            .andExpect(status().isNotFound());

        // 3. Start a non-existent match
        mockMvc.perform(patch("/api/v1/matches/999/startGame"))
            .andExpect(status().isNotFound());
    }
}