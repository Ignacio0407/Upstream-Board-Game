package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileService;
import es.us.dp1.l4_01_24_25.upstream.tile.TileType;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@WebMvcTest(controllers = MatchTileController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class))
@AutoConfigureMockMvc(addFilters = false)
class MatchTileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MatchTileService matchTileService;

    @MockBean
    private TileService tileService;

    @MockBean
    private MatchService matchService;

    @MockBean
    private UserService userService;

    private MatchTile testMatchTile;
    private Match testMatch;
    private Tile testTile;
    private TileType testType;

    @BeforeEach
    void setup() {
        testMatch = new Match();
        testMatch.setId(1);
        testMatch.setPlayersNum(2);
        testMatch.setRound(0);

        testTile = new Tile();
        testTile.setId(1);
        testType = new TileType();
        testType.setType("AGUA");
        testTile.setType(testType);

        testMatchTile = new MatchTile();
        testMatchTile.setId(1);
        testMatchTile.setMatch(testMatch);
        testMatchTile.setTile(testTile);
        testMatchTile.setOrientation(0);
        testMatchTile.setCapacity(2);
        testMatchTile.setSalmonsNumber(0);
        testMatchTile.setCoordinate(new Coordinate(null, null));
    }

    @Test
    void shouldGetAllMatchTiles() throws Exception {
        List<MatchTile> matchTiles = List.of(testMatchTile);
        when(matchTileService.findAll()).thenReturn(matchTiles);

        mockMvc.perform(get("/api/v1/matchTiles"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldUpdateMatchTileSuccessfully() throws Exception {
        Map<String, Integer> updates = new HashMap<>();
        updates.put("x", 1);
        updates.put("y", 1);

        when(matchTileService.findById(1)).thenReturn(testMatchTile);
        when(matchTileService.findByMatchId(any())).thenReturn(new ArrayList<>());
        when(matchTileService.save(any(MatchTile.class))).thenReturn(testMatchTile);

        mockMvc.perform(patch("/api/v1/matchTiles/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(updates)))
               .andExpect(status().isOk());
    }

    @Test
    void shouldFailUpdateMatchTileWhenPositionOccupied() throws Exception {
        Map<String, Integer> updates = new HashMap<>();
        updates.put("x", 1);
        updates.put("y", 1);

        MatchTile occupyingTile = new MatchTile();
        occupyingTile.setCoordinate(new Coordinate(1, 1));
        
        when(matchTileService.findById(1)).thenReturn(testMatchTile);
        when(matchTileService.findByMatchId(any())).thenReturn(List.of(occupyingTile));

        mockMvc.perform(patch("/api/v1/matchTiles/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(updates)))
               .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldGetMatchTilesByMatchId() throws Exception {
        List<MatchTile> matchTiles = List.of(testMatchTile);
        when(matchTileService.findByMatchId(1)).thenReturn(matchTiles);

        mockMvc.perform(get("/api/v1/matchTiles/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldCreateMatchTile() throws Exception {
        when(matchTileService.save(any(MatchTile.class))).thenReturn(testMatchTile);

        mockMvc.perform(post("/api/v1/matchTiles")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(testMatchTile)))
               .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteMatchTile() throws Exception {
        when(matchTileService.findById(1)).thenReturn(testMatchTile);

        mockMvc.perform(delete("/api/v1/matchTiles/1"))
               .andExpect(status().isOk());

        verify(matchTileService).deleteMatchTile(1);
    }

    @Test
    void shouldUpdateMatchTileRotation() throws Exception {
        when(matchTileService.findById(1)).thenReturn(testMatchTile);
        when(matchTileService.save(any(MatchTile.class))).thenReturn(testMatchTile);

        mockMvc.perform(patch("/api/v1/matchTiles/1/rotation")
               .contentType(MediaType.APPLICATION_JSON)
               .content("90"))
               .andExpect(status().isOk());
    }

    @Test
    void shouldCreateMultipleMatchTiles() throws Exception {
        when(tileService.findById(1)).thenReturn(Optional.of(testTile));
        when(tileService.findById(2)).thenReturn(Optional.of(testTile));
        when(tileService.findById(3)).thenReturn(Optional.of(testTile));
        when(tileService.findById(4)).thenReturn(Optional.of(testTile));
        when(tileService.findById(5)).thenReturn(Optional.of(testTile));
        when(tileService.findById(6)).thenReturn(Optional.of(testTile));
        when(matchService.getById(1)).thenReturn(testMatch);
        when(matchTileService.save(any(MatchTile.class))).thenReturn(testMatchTile);

        mockMvc.perform(post("/api/v1/matchTiles/createMatchTiles/1"))
               .andExpect(status().isCreated());
    }

    @Test
    void shouldFailCreateMultipleMatchTilesWhenMatchNotFound() throws Exception {
        when(tileService.findById(1)).thenReturn(Optional.of(testTile));
        when(tileService.findById(2)).thenReturn(Optional.of(testTile));
        when(matchService.getById(1)).thenReturn(null);

        mockMvc.perform(post("/api/v1/matchTiles/createMatchTiles/1"))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetMatchTilesWithoutCoordinates() throws Exception {
        List<MatchTile> matchTiles = List.of(testMatchTile);
        when(matchTileService.findByMatchIdNoCoord(1)).thenReturn(matchTiles);

        mockMvc.perform(get("/api/v1/matchTiles/prueba1/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1));
    }
}