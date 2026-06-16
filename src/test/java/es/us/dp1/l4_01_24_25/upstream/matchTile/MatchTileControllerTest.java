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

@WebMvcTest(controllers = MatchTileController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class))
@AutoConfigureMockMvc(addFilters = false)
public class MatchTileControllerTest {

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

    private MatchTile testMatchTile;
    private Match testMatch;
    private Tile testTile;

    @BeforeEach
    @SuppressWarnings("unused")
    void setup() {
        testMatchTile = new MatchTile();
        testMatchTile.setId(1);
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
        Match match = new Match();
        match.setId(1);
        match.setRound(0);
        match.setNumJugadores(2);
        testMatchTile.setMatch(match);

        Map<String, Integer> updates = new HashMap<>();
        updates.put("x", 0); updates.put("y", 0);

        when(matchTileService.findById(1)).thenReturn(testMatchTile);
        when(matchTileService.findByMatchId(1)).thenReturn(new ArrayList<>());
        when(matchTileService.save(any(MatchTile.class))).thenReturn(testMatchTile);

        mockMvc.perform(patch("/api/v1/matchTiles/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updates)))
            .andExpect(status().isOk());
    }

    @Test
    void shouldFailUpdateMatchTileWhenPositionOccupied() throws Exception {
        Match match = new Match();
        match.setId(1);
        match.setRound(0);
        testMatchTile.setMatch(match);
        testTile = new Tile();
        testTile.setId(1);

        Map<String, Integer> updates = new HashMap<>();
        updates.put("x", 0);
        updates.put("y", 0);

        MatchTile occupyingTile = new MatchTile();
        occupyingTile.setCoordinate(new Coordinate(0, 0));
        
        when(matchTileService.findById(1)).thenReturn(testMatchTile);
        when(matchTileService.findByMatchId(any())).thenReturn(List.of(occupyingTile));

        mockMvc.perform(patch("/api/v1/matchTiles/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updates)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("Ya existe una MatchTile en las coordenadas especificadas."));
    }

    @Test
    void shouldFailWhenMatchTileNotFound() throws Exception {
        Map<String, Integer> updates = new HashMap<>();
        updates.put("x", 1);
        updates.put("y", 1);

        when(matchTileService.findById(1)).thenReturn(null); // Simula que el MatchTile no existe

        mockMvc.perform(patch("/api/v1/matchTiles/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updates)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("MatchTile not found with ID: '1'"));
    }

    @Test
    void shouldFailWhenMatchTileInSameRoundNotFound() throws Exception {
        // Setup match y matchTile
        Match match = new Match();
        match.setId(1);
        match.setRound(1);  // Se asegura de que la ronda sea 1
        testMatchTile.setMatch(match);

        Map<String, Integer> updates = new HashMap<>();
        updates.put("x", 1);
        updates.put("y", 1); // Cambia las coordenadas a y=1

        // Simula que no se encuentra un MatchTile con las coordenadas dadas
        when(matchTileService.findById(1)).thenReturn(testMatchTile);
        when(matchTileService.findByMatchId(1)).thenReturn(new ArrayList<>());
        
        mockMvc.perform(patch("/api/v1/matchTiles/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updates)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("No se puede actualizar el MatchTile en esta ronda not found with ID: '1'"));
    }

    @Test
    void shouldFailWhenUpdateNotAllowedInCurrentRound() throws Exception {
        // Setup match y matchTile
        Match match = new Match();
        match.setId(1);
        match.setRound(0);  // Ronda 0
        testMatchTile.setMatch(match);

        Map<String, Integer> updates = new HashMap<>();
        updates.put("x", 0);
        updates.put("y", 4);  // Coordenadas no permitidas en ronda 0

        when(matchTileService.findById(1)).thenReturn(testMatchTile);
        when(matchTileService.findByMatchId(1)).thenReturn(new ArrayList<>());

        mockMvc.perform(patch("/api/v1/matchTiles/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updates)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("No se puede actualizar el MatchTile en esta ronda not found with ID: '1'"));
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
        testMatch = new Match();
        testMatch.setId(1);
        testMatch.setNumJugadores(2);
        testTile = new Tile();
        testTile.setId(1);

        when(matchService.getById(1)).thenReturn(testMatch);
        for(int i = 1; i <= 6; i++) {
            when(tileService.findById(i)).thenReturn(Optional.of(testTile));
        }
        when(matchTileService.save(any(MatchTile.class))).thenReturn(testMatchTile);

        mockMvc.perform(post("/api/v1/matchTiles/createMatchTiles/1"))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldFailCreateMultipleMatchTilesWhenMatchNotFound() throws Exception {
        testMatch = new Match();
        testMatch.setId(1);
        testMatch.setNumJugadores(2);
        testTile = new Tile();
        testTile.setId(1);
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