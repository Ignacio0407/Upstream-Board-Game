package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.configuration.SecurityConfiguration;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;

@WebMvcTest(controllers = MatchRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
class MatchRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MatchService matchService;

    @MockBean
    private PlayerService playerService;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindAll_Positive() throws Exception {
        List<Match> partidas = Arrays.asList(new Match(), new Match());
        when(matchService.getAll()).thenReturn(partidas);
        
        mockMvc.perform(get("/api/v1/matches"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(partidas)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindAll_Negative() throws Exception {
        when(matchService.getAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/matches"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindSomeByName_Positive() throws Exception {
        List<String> nombres = Arrays.asList("Partida1", "Partida2");
        List<Match> partidas = Arrays.asList(new Match(), new Match());
        when(matchService.getSomeByName(nombres)).thenReturn(partidas);

        mockMvc.perform(get("/api/v1/matches/names/Partida1,Partida2"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(partidas)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindSomeByName_Negative() throws Exception {
        when(matchService.getSomeByName(any()))
            .thenThrow(new ResourceNotFoundException("Una o más partidas no encontradas"));

        mockMvc.perform(get("/api/v1/matches/names/Partida1,NoExiste"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindById_Positive() throws Exception {
        Match partida = new Match();
        when(matchService.getById(1)).thenReturn(partida);

        mockMvc.perform(get("/api/v1/matches/1"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(partida)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindById_Negative() throws Exception {
        when(matchService.getById(1)).thenReturn(null);

        mockMvc.perform(get("/api/v1/matches/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindByName_Positive() throws Exception {
        Match partida = new Match();
        partida.setName("Partida1");
        when(matchService.geByName("Partida1")).thenReturn(partida);

        mockMvc.perform(get("/api/v1/matches/name/Partida1"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(partida)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindByName_Negative() throws Exception {
        when(matchService.geByName("NonExistent"))
            .thenThrow(new ResourceNotFoundException("Partida no encontrada"));

        mockMvc.perform(get("/api/v1/matches/name/NonExistent"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindPlayersFromGame_Positive() throws Exception {
        List<Player> jugadores = Arrays.asList(new Player(), new Player());
        when(matchService.getPlayersFromGame(1)).thenReturn(jugadores);

        mockMvc.perform(get("/api/v1/matches/1/players"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(jugadores)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindPlayersFromGame_Negative() throws Exception {
        when(matchService.getPlayersFromGame(1)).thenReturn(null);

        mockMvc.perform(get("/api/v1/matches/1/players"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testCreate_Positive() throws Exception {
        Match partida = new Match();
        partida.setName("Test Match");
        partida.setPlayersNum(1);
        when(matchService.save(any(Match.class))).thenReturn(partida);

        mockMvc.perform(post("/api/v1/matches")
        .with(csrf())
        .with(user("testUser").roles("USER"))
        .contentType(MediaType.APPLICATION_JSON) // Asegura el tipo de contenido
        .content(objectMapper.writeValueAsString(partida)))
        .andExpect(status().isCreated())
        .andExpect(content().json(objectMapper.writeValueAsString(partida)));

    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testCreate_Negative() throws Exception {
        Match partidaInvalida = new Match(); // Sin los campos requeridos
        partidaInvalida.setId(1);
        partidaInvalida.setName("as");
        
        mockMvc.perform(post("/api/v1/matches")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(partidaInvalida)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteById_Positive() throws Exception {
        Match partida = new Match();
        when(matchService.getById(1)).thenReturn(partida);

        mockMvc.perform(delete("/api/v1/matches/1")
        .with(csrf())
        .with(user("testUser").roles("ADMIN")))
        .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteById_Negative() throws Exception {
        doThrow(new ResourceNotFoundException("Partida no encontrada"))
            .when(matchService).getById(1);

        mockMvc.perform(delete("/api/v1/matches/1")
        .with(csrf())
        .with(user("testUser").roles("ADMIN")))
            .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteSomeById_Positive() throws Exception {
        when(matchService.getById(anyInt())).thenReturn(new Match());

        mockMvc.perform(delete("/api/v1/matches/ids/1,2,3")
        .with(csrf())
        .with(user("testUser").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Partidas borradas"));
    }

    @Test
    void testDeleteSomeById_Negative() throws Exception {
        doThrow(new ResourceNotFoundException("Una o más partidas no encontradas"))
            .when(matchService).deleteSomeById(any());

        mockMvc.perform(delete("/api/v1/matches/ids/1,99")
                .with(csrf())
                .with(user("testUser").roles("ADMIN")))
            .andExpect(status().isNotFound());
    }

    @Test
    void testUpdate_Positive() throws Exception {
        Match partida = new Match();
        partida.setName("Updated Match");
        partida.setId(1);
        when(matchService.getById(1)).thenReturn(partida);
        when(matchService.updateById(any(Match.class), anyInt())).thenReturn(partida);

        mockMvc.perform(put("/api/v1/matches/1")
                .with(csrf()) 
                .with(user("testUser").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partida))) // Agrega el contenido JSON
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(partida)));
    }


    @Test
    @WithMockUser(roles = {"USER"})
    void testUpdate_Negative() throws Exception {
        doThrow(new ResourceNotFoundException("Partida no encontrada"))
            .when(matchService).getById(1);

        mockMvc.perform(put("/api/v1/matches/1")
            .with(csrf())
            .with(user("testUser").roles("USER"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new Match())))
            .andExpect(status().isNotFound());
    }


    @Test
    void testUpdateJugadorActual_Positive() throws Exception {
        Match partida = new Match();
        Player jugador = new Player();
        when(matchService.getById(1)).thenReturn(partida);
        when(playerService.getJugadorById(1)).thenReturn(jugador);
        when(matchService.save(any(Match.class))).thenReturn(partida);

        mockMvc.perform(patch("/api/v1/matches/1/actualPlayer/1")
                .with(csrf())
                .with(user("testUser").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(partida)));
    }

    @Test
    void testUpdateJugadorActual_MatchNotFound() throws Exception {
        when(matchService.getById(1))
            .thenThrow(new ResourceNotFoundException("Partida no encontrada"));

        mockMvc.perform(patch("/api/v1/matches/1/actualPlayer/1")
                .with(csrf())
                .with(user("testUser").roles("ADMIN")))
            .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateJugadorActual_PlayerNotFound() throws Exception {
        Match partida = new Match();
        when(matchService.getById(1)).thenReturn(partida);
        when(playerService.getJugadorById(1))
            .thenThrow(new ResourceNotFoundException("Jugador no encontrado"));

        mockMvc.perform(patch("/api/v1/matches/1/actualPlayer/1")
                .with(csrf())
                .with(user("testUser").roles("ADMIN")))
            .andExpect(status().isNotFound());
    }

}