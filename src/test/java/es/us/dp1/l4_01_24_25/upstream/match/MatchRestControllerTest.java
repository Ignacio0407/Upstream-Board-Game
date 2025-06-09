package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.user.User;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class MatchRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MatchService matchService;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private MatchTileService matchTileService;


    private Match testMatch;

    @BeforeEach
    @SuppressWarnings("unused")
    void setup() {
        testMatch = new Match();
        testMatch.setId(1);
        testMatch.setName("Test Match");
        testMatch.setPlayersNum(2);
        testMatch.setRound(0);
        testMatch.setState(State.ESPERANDO);
        testMatch.setPhase(Phase.CASILLAS);
    }

    @Test
    void shouldGetAllMatches() throws Exception {
        List<Match> matches = List.of(testMatch);
        when(matchService.findAll()).thenReturn(matches);

        mockMvc.perform(get("/api/v1/matches"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1))
               .andExpect(jsonPath("$[0].name").value("Test Match"));
    }

    @Test
    void shouldFindMatchById() throws Exception {
        when(matchService.findById(1)).thenReturn(testMatch);

        mockMvc.perform(get("/api/v1/matches/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.name").value("Test Match"));
    }

    @Test
    void shouldReturnNotFoundWhenMatchDoesNotExist() throws Exception {
        when(matchService.findById(1)).thenReturn(null);

        mockMvc.perform(get("/api/v1/matches/1"))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateMatchSuccessfully() throws Exception {
        Player player = new Player();
        player.setId(1);
        player.setName("Player 1");
        player.setEnergy(5);
        player.setAlive(true);
        User user = new User();
        user.setId(1);
        user.setName("Paco");
        testMatch.setInitialPlayer(player);
        testMatch.setMatchCreator(user);
        testMatch.setSalmonMatches(new ArrayList<>());
        when(matchService.save(any(Match.class))).thenReturn(testMatch);

        mockMvc.perform(post("/api/v1/matches")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testMatch)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.name").value("Test Match"));
    }

    @Test
    void shouldFailCreatingMatchWhenInvalidName() throws Exception {
        testMatch.setName("A");

        mockMvc.perform(post("/api/v1/matches")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testMatch)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateMatchSuccessfully() throws Exception {
        Match updatedMatch = new Match();
        updatedMatch.setId(1);
        updatedMatch.setName("Updated Match");
        updatedMatch.setPlayersNum(3);

        when(matchService.findById(1)).thenReturn(testMatch);
        when(matchService.updateById(any(Match.class), eq(1))).thenReturn(updatedMatch);

        mockMvc.perform(put("/api/v1/matches/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(updatedMatch)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.name").value("Updated Match"));
    }

    @Test
    void shouldFailUpdatingMatchWhenNotFound() throws Exception {
        Match updatedMatch = new Match();
        updatedMatch.setId(2);
        updatedMatch.setName("Updated Match");

        when(matchService.updateById(updatedMatch, testMatch.getId())).thenReturn(null);

        mockMvc.perform(put("/api/v1/matches/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(updatedMatch)))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteMatchSuccessfully() throws Exception {
        when(matchService.findById(1)).thenReturn(testMatch);

        mockMvc.perform(delete("/api/v1/matches/1"))
               .andExpect(status().isNoContent());

        verify(matchService).delete(1);
    }

    @Test
    void shouldFailDeletingMatchWhenNotFound() throws Exception {
        when(matchService.findById(1)).thenReturn(null);

        mockMvc.perform(delete("/api/v1/matches/1"))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldStartGameSuccessfully() throws Exception {
        Player player = new Player();
        player.setId(1);
        player.setName("Player 1");
        player.setEnergy(5);
        player.setAlive(true);
        testMatch.setInitialPlayer(player);

        when(matchService.findById(1)).thenReturn(testMatch);
        when(playerService.findPlayersByMatch(1)).thenReturn(List.of(player));

        mockMvc.perform(patch("/api/v1/matches/1/startGame"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("EN_CURSO"));
    }


    @Test
    void shouldFailStartGameWhenMatchNotFound() throws Exception {
        when(matchService.findById(1)).thenReturn(null);

        mockMvc.perform(patch("/api/v1/matches/1/startGame"))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldChangePhaseSuccessfully() throws Exception {
        List<Player> players = List.of(new Player());
        when(matchService.findById(1)).thenReturn(testMatch);
        when(playerService.findById(1)).thenReturn(players.get(0));
        when(matchTileService.findByMatchIdNoCoord(1)).thenReturn(List.of());

        mockMvc.perform(patch("/api/v1/matches/1/changephase/1"))
               .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenChangingPhaseWithInvalidMatch() throws Exception {
        when(matchService.findById(1)).thenReturn(null);

        mockMvc.perform(patch("/api/v1/matches/1/changephase/1"))
               .andExpect(status().isNotFound());
    }
}