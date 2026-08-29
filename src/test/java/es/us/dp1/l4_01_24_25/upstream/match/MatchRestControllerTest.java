package es.us.dp1.l4_01_24_25.upstream.match;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ConflictException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.dto.DashboardMatchDTO;
import es.us.dp1.l4_01_24_25.upstream.match.dto.MatchDTO;

@WebMvcTest(controllers = MatchRestController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class))
@AutoConfigureMockMvc(addFilters = false)
class MatchRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MatchService matchService;

    @Test
    @DisplayName("GET /dashboard - Happy Path")
    void shouldFindAllDashboard() throws Exception {
        DashboardMatchDTO dto = new DashboardMatchDTO(1, "Match 1", "pass", State.WAITING, 0);
        when(matchService.findAllAsDashboardDTO()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/matches/dashboard"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].name").value("Match 1"));

        verify(matchService).findAllAsDashboardDTO();
    }

    @Test
    @DisplayName("GET /dashboard - Edge Case (Empty List)")
    void shouldReturnEmptyListIfNoMatches() throws Exception {
        when(matchService.findAllAsDashboardDTO()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/matches/dashboard"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isEmpty());
    }



    @Test
    @DisplayName("POST /matchCreator/{userId} - Happy Path")
    void shouldCreateMatchWMatchCreator() throws Exception {
        MatchDTO dto = new MatchDTO();
        dto.setId(1);
        dto.setName("New Match");

        Map<String, String> body = Map.of("name", "New Match", "password", "pass");

        when(matchService.createMatchWMatchCreator(eq(1), any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/matches/matchCreator/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(body)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.name").value("New Match"));

        verify(matchService).createMatchWMatchCreator(eq(1), any());
    }

    @Test
    @DisplayName("POST /matchCreator/{userId} - Negative Path (User Not Found -> 404)")
    void shouldFailCreateMatchWhenUserNotFound() throws Exception {
        Map<String, String> body = Map.of("name", "New Match");

        when(matchService.createMatchWMatchCreator(eq(99), any()))
            .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(post("/api/v1/matches/matchCreator/99")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(body)))
               .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /matchCreator/{userId} - Edge Case (Malformed JSON -> 400)")
    void shouldFailCreateMatchOnMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/matches/matchCreator/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{ \"name\": \"Broken JSON ")) // JSON Roto
               .andExpect(status().isBadRequest());

        verify(matchService, never()).createMatchWMatchCreator(any(), any());
    }

    @Test
    @DisplayName("POST /matchCreator/{userId} - Edge Case (Type Mismatch on URL -> 400)")
    void shouldFailCreateMatchOnTypeMismatch() throws Exception {
        mockMvc.perform(post("/api/v1/matches/matchCreator/abc")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{}"))
               .andExpect(status().isBadRequest());

        verify(matchService, never()).createMatchWMatchCreator(any(), any());
    }


    @Test
    @DisplayName("PATCH /{matchId}/startGame - Happy Path")
    void shouldStartGame() throws Exception {
        doNothing().when(matchService).startGame(1);

        mockMvc.perform(patch("/api/v1/matches/1/startGame"))
               .andExpect(status().isOk());

        verify(matchService).startGame(1);
    }

    @Test
    @DisplayName("PATCH /{matchId}/startGame - Negative Path (Match Not Found -> 404)")
    void shouldFailStartGameWhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Match", "id", 999)).when(matchService).startGame(999);

        mockMvc.perform(patch("/api/v1/matches/999/startGame"))
               .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /{matchId}/startGame - Edge Case (Type Mismatch -> 400)")
    void shouldFailStartGameOnTypeMismatch() throws Exception {
        mockMvc.perform(patch("/api/v1/matches/abc/startGame"))
               .andExpect(status().isBadRequest());

        verify(matchService, never()).startGame(any());
    }


    @Test
    @DisplayName("PATCH /{matchId}/changephase/{playerId} - Happy Path")
    void shouldChangePhase() throws Exception {
        doNothing().when(matchService).changePhase(1, 2);

        mockMvc.perform(patch("/api/v1/matches/1/changephase/2"))
               .andExpect(status().isOk());

        verify(matchService).changePhase(1, 2);
    }

    @Test
    @DisplayName("PATCH /{matchId}/changephase/{playerId} - Negative Path (Not Turn -> 400)")
    void shouldFailChangePhaseWhenNotPlayersTurn() throws Exception {
        doThrow(new NotValidMoveException("It is not your turn!")).when(matchService).changePhase(1, 2);

        mockMvc.perform(patch("/api/v1/matches/1/changephase/2"))
               .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /{matchId}/changephase/{playerId} - Edge Case (Type Mismatch -> 400)")
    void shouldFailChangePhaseOnTypeMismatch() throws Exception {
        mockMvc.perform(patch("/api/v1/matches/1/changephase/abc"))
               .andExpect(status().isBadRequest());

        verify(matchService, never()).changePhase(any(), any());
    }


    @Test
    @DisplayName("PATCH /finalscore/{id} - Happy Path")
    void shouldCalculateFinalScore() throws Exception {
        MatchDTO dto = new MatchDTO();
        dto.setId(1);
        dto.setFinalScoreCalculated(true);

        when(matchService.finalScore(1)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/matches/finalscore/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.finalScoreCalculated").value(true));
    }

    @Test
    @DisplayName("PATCH /finalscore/{id} - Negative Path (Already Calculated -> 409)")
    void shouldFailFinalScoreWhenConflict() throws Exception {
        when(matchService.finalScore(1))
            .thenThrow(new ConflictException("Final score for this match has already been calculated"));

        mockMvc.perform(patch("/api/v1/matches/finalscore/1"))
               .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PATCH /finalscore/{id} - Edge Case (Type Mismatch -> 400)")
    void shouldFailFinalScoreOnTypeMismatch() throws Exception {
        mockMvc.perform(patch("/api/v1/matches/finalscore/invalidId"))
               .andExpect(status().isBadRequest());

        verify(matchService, never()).finalScore(any());
    }
}