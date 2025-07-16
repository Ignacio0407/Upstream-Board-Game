package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.InsufficientEnergyException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NoCapacityException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.match.Phase;
import es.us.dp1.l4_01_24_25.upstream.match.State;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.player.Color;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import es.us.dp1.l4_01_24_25.upstream.salmon.SalmonService;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileType;

@WebMvcTest(controllers = SalmonMatchController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class))
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("unused")
public class SalmonMatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SalmonMatchService salmonMatchService;
    @MockBean
    private PlayerService playerService;
    @MockBean
    private SalmonService salmonService;
    @MockBean
    private MatchTileService matchTileService;
    @MockBean
    private MatchService matchService;

    @Autowired
    private ObjectMapper objectMapper;

    private SalmonMatch salmonMatch;
    private Player player;
    private Match match;
    private MatchTile matchTile;
    private Salmon salmon;
    private Tile tile;

    @BeforeEach
    void setup() {
        player = new Player();
        player.setId(1);
        player.setName("Player 1");
        player.setEnergy(5);
        player.setColor(Color.RED);
        player.setPlayerOrder(0);
        player.setAlive(true);
        player.setPoints(0);
        // userPlayer can be null for basic tests unless specifically needed

        match = new Match();
        match.setId(1);
        match.setPlayersNumber(2);
        match.setState(State.ON_GOING); 
        match.setRound(1);
        match.setPhase(Phase.MOVING);
        match.setActualPlayer(player);
        match.setInitialPlayer(player);
        match.setPassword("testpass");

        player.setMatch(match);

        tile = new Tile();
        tile.setId(1);
        tile.setImage("AGUA");

        matchTile = new MatchTile();
        matchTile.setId(1);
        matchTile.setTile(tile);
        matchTile.setCoordinate(new Coordinate(1, 0));
        matchTile.setCapacity(2);
        matchTile.setSalmonsNumber(0);
        matchTile.setOrientation(0);
        matchTile.setMatch(match);

        salmon = new Salmon();
        salmon.setId(1);
        salmon.setImage("rojo2");

        salmonMatch = new SalmonMatch();
        salmonMatch.setId(1);
        salmonMatch.setPlayer(player);
        salmonMatch.setMatch(match);
        salmonMatch.setSalmon(salmon);
        salmonMatch.setSalmonsNumber(2);
        salmonMatch.setCoordinate(new Coordinate(null, null));
    }

    @Test
    void shouldFindAllFromMatch() throws Exception {
        when(salmonMatchService.findAllFromMatch(anyInt())).thenReturn(List.of(salmonMatch));

        mockMvc.perform(get("/api/v1/salmonMatches/match/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldFindAllFromMatchInSpawn() throws Exception {
        when(salmonMatchService.findFromGameInSpawn(anyInt())).thenReturn(List.of(salmonMatch));

        mockMvc.perform(get("/api/v1/salmonMatches/match/1/spawn"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldFindById() throws Exception {
        when(salmonMatchService.findById(anyInt())).thenReturn(salmonMatch);

        mockMvc.perform(get("/api/v1/salmonMatches/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldCreateSalmonMatch() throws Exception {
        when(salmonMatchService.save(any(SalmonMatch.class))).thenReturn(salmonMatch);

        mockMvc.perform(post("/api/v1/salmonMatches")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(salmonMatch)))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldDeleteSalmonMatch() throws Exception {
        mockMvc.perform(delete("/api/v1/salmonMatches/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldUpdateCoordinateFromSea() throws Exception {
        // Setup for move from sea
        salmonMatch.setCoordinate(null);
        when(salmonMatchService.findById(anyInt())).thenReturn(salmonMatch);
        when(matchTileService.findByMatchId(anyInt())).thenReturn(List.of(matchTile));
        when(playerService.findPlayersByMatch(anyInt())).thenReturn(List.of(player));

        Map<String, Integer> newCoord = Map.of("x", 1, "y", 0);

        mockMvc.perform(patch("/api/v1/salmonMatches/coordinate/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newCoord)))
            .andExpect(status().isOk());
    }

    @Test
    void shouldFailUpdateCoordinateNoEnergy() throws Exception {
        player.setEnergy(0);
        when(salmonMatchService.findById(anyInt())).thenReturn(salmonMatch);

        Map<String, Integer> newCoord = Map.of("x", 1, "y", 0);

        mockMvc.perform(patch("/api/v1/salmonMatches/coordinate/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newCoord)))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof InsufficientEnergyException));
    }

    @Test
    void shouldFailUpdateCoordinateNoCapacity() throws Exception {
        matchTile.setSalmonsNumber(2);
        when(salmonMatchService.findById(anyInt())).thenReturn(salmonMatch);
        when(matchTileService.findByMatchId(anyInt())).thenReturn(List.of(matchTile));
        when(salmonMatchService.findAllFromMatch(anyInt())).thenReturn(List.of(salmonMatch, salmonMatch, salmonMatch));

        Map<String, Integer> newCoord = Map.of("x", 1, "y", 0);

        mockMvc.perform(patch("/api/v1/salmonMatches/coordinate/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newCoord)))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NoCapacityException));
    }

    @Test
    void shouldFailUpdateCoordinateInvalidMove() throws Exception {
        when(salmonMatchService.findById(anyInt())).thenReturn(salmonMatch);
        when(matchTileService.findByMatchId(anyInt())).thenReturn(List.of(matchTile));

        Map<String, Integer> newCoord = Map.of("x", 1, "y", 1); // Moving backwards

        mockMvc.perform(patch("/api/v1/salmonMatches/coordinate/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newCoord)))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotValidMoveException));
    }

    @Test
    void shouldHandleOsoBearEncounter() throws Exception {
        matchTile.setOrientation(0);
        
        when(salmonMatchService.findById(anyInt())).thenReturn(salmonMatch);
        when(matchTileService.findByMatchId(anyInt())).thenReturn(List.of(matchTile));
        when(playerService.findPlayersByMatch(anyInt())).thenReturn(List.of(player));
        when(salmonMatchService.findAllFromMatch(anyInt())).thenReturn(List.of());

        Map<String, Integer> newCoord = Map.of("x", 1, "y", 0);

        mockMvc.perform(patch("/api/v1/salmonMatches/coordinate/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newCoord)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.salmonsNumber").value(1)); // Expect salmon number to decrease
    }

    @Test
    void shouldHandleEagleEncounter() throws Exception {
        
        when(salmonMatchService.findById(anyInt())).thenReturn(salmonMatch);
        when(matchTileService.findByMatchId(anyInt())).thenReturn(List.of(matchTile));
        when(matchTileService.eagleToWater(any())).thenReturn(matchTile);
        when(playerService.findPlayersByMatch(anyInt())).thenReturn(List.of(player));
        when(salmonMatchService.findAllFromMatch(anyInt())).thenReturn(List.of());

        Map<String, Integer> newCoord = Map.of("x", 1, "y", 0);

        mockMvc.perform(patch("/api/v1/salmonMatches/coordinate/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newCoord)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.salmonsNumber").value(1));
    }

    @Test
    void shouldHandleJumpTile() throws Exception {
        matchTile.setOrientation(0);
        
        when(salmonMatchService.findById(anyInt())).thenReturn(salmonMatch);
        when(matchTileService.findByMatchId(anyInt())).thenReturn(List.of(matchTile));
        when(playerService.findPlayersByMatch(anyInt())).thenReturn(List.of(player));
        when(salmonMatchService.findAllFromMatch(anyInt())).thenReturn(List.of());

        Map<String, Integer> newCoord = Map.of("x", 1, "y", 0);

        mockMvc.perform(patch("/api/v1/salmonMatches/coordinate/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newCoord)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.salmonsNumber").value(2)); // Salmon number shouldn't change
    }

    @Test
    void shouldHandleEnterSpawn() throws Exception {
        salmonMatch.setCoordinate(new Coordinate(1, 4));
        when(salmonMatchService.findById(anyInt())).thenReturn(salmonMatch);
        when(matchTileService.findByMatchId(anyInt())).thenReturn(List.of(matchTile));
        when(playerService.findPlayersByMatch(anyInt())).thenReturn(List.of(player));

        mockMvc.perform(patch("/api/v1/salmonMatches/enterSpawn/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.coordinate.y").value(21));
    }

    @Test
    void shouldCreateSalmonMatchesForPlayer() throws Exception {
        when(playerService.findById(anyInt())).thenReturn(player);
        when(salmonService.findAll()).thenReturn(List.of(salmon));

        mockMvc.perform(post("/api/v1/salmonMatches/player/1"))
            .andExpect(status().isOk());
    }
}