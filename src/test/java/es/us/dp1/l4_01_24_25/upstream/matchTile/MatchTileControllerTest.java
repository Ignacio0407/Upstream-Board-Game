package es.us.dp1.l4_01_24_25.upstream.matchTile;

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
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.configuration.SecurityConfiguration;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ConflictException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MatchTileDTO;

@WebMvcTest(controllers = MatchTileController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfiguration.class))
@AutoConfigureMockMvc(addFilters = false)
class MatchTileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MatchTileController matchTileController;

    @MockBean
    private MatchTileService matchTileService;

    // ── GET /match/{id} ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /match/{id} - Happy Path")
    void shouldGetMatchTilesByMatchId() throws Exception {
        MatchTileDTO dto = new MatchTileDTO();
        dto.setId(1);
        when(matchTileService.findByMatchIdAsDTO(1)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/matchTiles/match/{id}", 1))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1));

        verify(matchTileService).findByMatchIdAsDTO(1);
    }

    @Test
    @DisplayName("GET /match/{id} - Negative Path (Match Not Found -> 404)")
    void shouldReturnNotFoundWhenMatchDoesNotExist() throws Exception {
        when(matchTileService.findByMatchIdAsDTO(99))
            .thenThrow(new ResourceNotFoundException("Match not found"));

        mockMvc.perform(get("/api/v1/matchTiles/match/{id}", 99))
               .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /match/{id} - Edge Case (Type Mismatch -> 400)")
    void shouldReturnBadRequestOnMatchIdTypeMismatch() throws Exception {
        mockMvc.perform(get("/api/v1/matchTiles/match/invalidId"))
               .andExpect(status().isBadRequest());

        verify(matchTileService, never()).findByMatchIdAsDTO(any());
    }

    // ── PATCH /{id} ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /{id} - Happy Path (Update coordinates)")
    void shouldUpdateMatchTileSuccessfully() throws Exception {
        Map<String, Integer> updates = Map.of("x", 0, "y", 0);

        doNothing().when(matchTileService).updateCoordinate(eq(1), any());

        mockMvc.perform(patch("/api/v1/matchTiles/{id}", 1)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(updates)))
               .andExpect(status().isOk());
        
        verify(matchTileService).updateCoordinate(eq(1), any());
    }

    @Test
    @DisplayName("PATCH /{id} - Negative Path (Position Occupied -> 409)")
    void shouldFailUpdateWhenPositionOccupied() throws Exception {
        Map<String, Integer> updates = Map.of("x", 0, "y", 0);

        doThrow(new ConflictException("Position occupied"))
            .when(matchTileService).updateCoordinate(eq(1), any());

        mockMvc.perform(patch("/api/v1/matchTiles/{id}", 1)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(updates)))
               .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PATCH /{id} - Negative Path (Tile Not Found -> 404)")
    void shouldFailUpdateWhenTileNotFound() throws Exception {
        Map<String, Integer> updates = Map.of("x", 1, "y", 1);

        doThrow(new ResourceNotFoundException("Tile not found"))
            .when(matchTileService).updateCoordinate(eq(1), any());

        mockMvc.perform(patch("/api/v1/matchTiles/{id}", 1)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(updates)))
               .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /{id} - Edge Case (Malformed JSON -> 400)")
    void shouldFailUpdateOnMalformedJson() throws Exception {
        mockMvc.perform(patch("/api/v1/matchTiles/{id}", 1)
               .contentType(MediaType.APPLICATION_JSON)
               .content("{ \"x\": 1, \"y\": }")) // Broken JSON
               .andExpect(status().isBadRequest());

        verify(matchTileService, never()).updateCoordinate(any(), any());
    }

    // ── MESSAGE MAPPING /{id}/rotation ────────────────────────────────────────

    @Test
    @DisplayName("PATCH /{id}/rotate - Happy Path")
    void shouldBroadcastMatchTileRotation() throws Exception {
        doNothing().when(matchTileService).broadcastMatchTileRotation(1, 90);

        mockMvc.perform(patch("/api/v1/matchTiles/{id}/rotate", 1)
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"rotation\": 90}"))
               .andExpect(status().isOk());

        verify(matchTileService).broadcastMatchTileRotation(1, 90);
    }

    // ── POST /createMatchTiles/{matchId} ──────────────────────────────────────

    @Test
    @DisplayName("POST /createMatchTiles/{matchId} - Happy Path")
    void shouldCreateMultipleMatchTiles() throws Exception {
        doNothing().when(matchTileService).createMultipleMatchTiles(1);

        mockMvc.perform(post("/api/v1/matchTiles/createMatchTiles/{matchId}", 1))
               .andExpect(status().isCreated());

        verify(matchTileService).createMultipleMatchTiles(1);
    }

    @Test
    @DisplayName("POST /createMatchTiles/{matchId} - Negative Path (Match Not Found -> 404)")
    void shouldFailCreateMultipleWhenMatchNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Match not found"))
            .when(matchTileService).createMultipleMatchTiles(99);

        mockMvc.perform(post("/api/v1/matchTiles/createMatchTiles/{matchId}", 99))
               .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /createMatchTiles/{matchId} - Edge Case (Type Mismatch -> 400)")
    void shouldFailCreateMultipleOnTypeMismatch() throws Exception {
        mockMvc.perform(post("/api/v1/matchTiles/createMatchTiles/notAnInteger"))
               .andExpect(status().isBadRequest());

        verify(matchTileService, never()).createMultipleMatchTiles(any());
    }
}