package es.us.dp1.l4_01_24_25.upstream.player;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
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

import es.us.dp1.l4_01_24_25.upstream.exceptions.InsufficientEnergyException;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.LobbyPlayerDTO;
import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@WebMvcTest(controllers = PlayerRestController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class))
@AutoConfigureMockMvc(addFilters = false)
class PlayerRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private UserService userService;

    @MockBean
    private MatchService matchService;

    @Test
    @DisplayName("DELETE /api/v1/players/{id} - Happy Path")
    void shouldDeletePlayer() throws Exception {
        doNothing().when(playerService).deleteById(1);

        mockMvc.perform(delete("/api/v1/players/{id}", 1))
               .andExpect(status().isNoContent());

        verify(playerService).deleteById(1);
    }

    @Test
    @DisplayName("GET /api/v1/players/match/{id} - Happy Path")
    void shouldReturnPlayersByMatchId() throws Exception {
        PlayerDTO dto = new PlayerDTO();
        dto.setId(1);
        when(playerService.findPlayersByMatchAsDTO(1)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/players/match/{id}", 1))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/players/match/sorted/{id} - Happy Path")
    void shouldReturnPlayersByMatchIdSorted() throws Exception {
        PlayerDTO dto = new PlayerDTO();
        dto.setId(2);
        when(playerService.findPlayersByMatchSortedPlayerOrderAsDTO(1)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/players/match/sorted/{id}", 1))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @DisplayName("POST /api/v1/players/match/{id} - Happy Path")
    void shouldCreatePlayerInMatch() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("user", "1");
        requestBody.put("color", "RED");

        LobbyPlayerDTO lobbyDto = new LobbyPlayerDTO();
        lobbyDto.setMatchId(1);
        
        when(playerService.createPlayerInMatch(eq(1), any())).thenReturn(lobbyDto);

        mockMvc.perform(post("/api/v1/players/match/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.matchId").value(1));
    }

    @Test
    @DisplayName("PATCH /api/v1/players/{id}/energy - Happy Path")
    void shouldUpdatePlayerEnergyWhenSufficient() throws Exception {
        PlayerDTO dto = new PlayerDTO();
        dto.setEnergy(5);
        when(playerService.updateEnergy(eq(1), eq(3))).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/players/{id}/energy", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("3")) // We pass a valid number
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.energy").value(5));
    }

    @Test
    @DisplayName("PATCH /api/v1/players/{id}/energy - Negative Path (Insufficient Energy -> 400)")
    void shouldReturnBadRequestWhenEnergyInsufficient() throws Exception {
        when(playerService.updateEnergy(eq(1), eq(10)))
            .thenThrow(new InsufficientEnergyException("Not enough energy"));

        mockMvc.perform(patch("/api/v1/players/{id}/energy", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("10"))
               .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/players/{id}/energy - Edge Case (Type Mismatch/Bad JSON -> 400)")
    void shouldReturnBadRequestOnEnergyTypeMismatch() throws Exception {
        mockMvc.perform(patch("/api/v1/players/{id}/energy", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"five\"")) //  String instead of Integer
               .andExpect(status().isBadRequest());

        verify(playerService, never()).updateEnergy(any(), any());
    }
}