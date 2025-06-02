package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@WebMvcTest(controllers = PlayerRestController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class))
@AutoConfigureMockMvc(addFilters = false)
class PlayerRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private UserService userService;

    @MockBean
    private MatchService matchService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllPlayers() throws Exception {
        List<Player> players = List.of(new Player());
        when(playerService.findAll()).thenReturn(players);

        mockMvc.perform(get("/api/v1/players"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void shouldReturnPlayersByName() throws Exception {
        List<Player> players = List.of(new Player());
        List<String> names = List.of("John", "Jane");
        //when(playerService.getSomeByName(names)).thenReturn(players);

        mockMvc.perform(get("/api/v1/players/names/{names}", "John,Jane"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void shouldReturnPlayerByIdWhenExists() throws Exception {
        Player player = new Player();
        player.setId(1);
        when(playerService.findById(1)).thenReturn(player);

        mockMvc.perform(get("/api/v1/players/{id}", 1))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenPlayerIdDoesNotExist() throws Exception {
        when(playerService.findById(99)).thenReturn(null);

        mockMvc.perform(get("/api/v1/players/{id}", 99))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnPlayersByMatchId() throws Exception {
        List<Player> players = List.of(new Player());
        when(playerService.findPlayersByMatch(1)).thenReturn(players);

        mockMvc.perform(get("/api/v1/players/match/{id}", 1))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void shouldReturnPlayerByNameWhenExists() throws Exception {
        Player player = new Player();
        player.setName("John");
        //when(playerService.getByName("John")).thenReturn(player);

        mockMvc.perform(get("/api/v1/players/name/{name}", "John"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void shouldReturnNotFoundWhenPlayerByNameDoesNotExist() throws Exception {
        //when(playerService.getByName("NonExistentName")).thenReturn(null);

        mockMvc.perform(get("/api/v1/players/name/{name}", "NonExistentName"))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenPlayerByIdDoesNotExistForDeletion() throws Exception {
        when(playerService.findById(99)).thenReturn(null);

        mockMvc.perform(delete("/api/v1/players/{id}", 99))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdatePlayerById() throws Exception {
        Player player = new Player();
        player.setId(1);
        when(playerService.findById(1)).thenReturn(player);
        when(playerService.updateById(any(), any())).thenReturn(player);

        mockMvc.perform(put("/api/v1/players/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(player)))
               .andExpect(status().isOk());
    }

    @Test
    void shouldCreatePlayer() throws Exception {
        Player player = new Player();
        when(playerService.save(any(Player.class))).thenReturn(player);

        mockMvc.perform(post("/api/v1/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(player)))
               .andExpect(status().isCreated());
    }

    @Test
    void shouldCreatePlayerInMatch() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("user", "1");
        requestBody.put("color", "RED");

        Player player = new Player();
        when(matchService.findById(1)).thenReturn(new Match());
        when(userService.findById(1)).thenReturn(new User());
        when(playerService.save(any(Player.class))).thenReturn(player);

        mockMvc.perform(post("/api/v1/players/match/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
               .andExpect(status().isCreated());
    }

    @Test
    void shouldUpdatePlayerEnergyWhenSufficient() throws Exception {
        Player player = new Player();
        player.setEnergy(10);
        when(playerService.findById(1)).thenReturn(player);
        when(playerService.save(any(Player.class))).thenReturn(player);

        mockMvc.perform(patch("/api/v1/players/{id}/energy", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(5)))
               .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenInsufficientEnergy() throws Exception {
        Player player = new Player();
        player.setEnergy(2);
        when(playerService.findById(1)).thenReturn(player);

        mockMvc.perform(patch("/api/v1/players/{id}/energy", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(5)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRegenerateEnergy() throws Exception {
        Player player = new Player();
        player.setEnergy(2);
        when(playerService.findById(1)).thenReturn(player);
        when(playerService.save(any(Player.class))).thenReturn(player);

        mockMvc.perform(patch("/api/v1/players/{id}/regenerateEnergy", 1))
               .andExpect(status().isOk());
    }
}

