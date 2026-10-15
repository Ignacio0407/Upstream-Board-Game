package upstream.player;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

import upstream.exceptions.AccessDeniedException;
import upstream.match.MatchService;
import upstream.player.playerdto.LobbyPlayerDTO;
import upstream.player.playerdto.PlayerDTO;
import upstream.user.UserService;

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
    @DisplayName("PATCH /api/v1/players/{id}/energy - Happy Path (Set energy to zero)")
    void shouldSetPlayerEnergyToZero() throws Exception {
        doNothing().when(playerService).setEnergyToZero(1);

        mockMvc.perform(patch("/api/v1/players/{id}/energy", 1)) // Ya no enviamos body
               .andExpect(status().isOk());

        verify(playerService).setEnergyToZero(1);
    }

    @Test
    @DisplayName("PATCH /api/v1/players/{id}/energy - Edge Case (Not player turn -> 403)")
    void shouldReturnBadRequestOnEnergyTypeMismatch() throws Exception {
        doThrow(new AccessDeniedException("It is not your turn!")).when(playerService).setEnergyToZero(1);

        mockMvc.perform(patch("/api/v1/players/{id}/energy", 1))
               .andExpect(status().isForbidden());

        verify(playerService).setEnergyToZero(1);
    }
}