package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.player.Color;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmon.SalmonService;

@WebMvcTest(controllers = SalmonMatchController.class)
class SalmonMatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private salmonMatchService salmonMatchService;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private SalmonService salmonService;

    @Test
    void testFindAllFromMatch_Positive() throws Exception {
        List<SalmonMatch> salmonMatches = Arrays.asList(new SalmonMatch(), new SalmonMatch());
        when(salmonMatchService.getAllFromMatch(anyInt())).thenReturn(salmonMatches);

        mockMvc.perform(get("/api/v1/salmonMatches/match/1")
                .with(user("testUser").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(salmonMatches)));
    }

    @Test
    void testFindAllFromPlayer_Positive() throws Exception {
        List<SalmonMatch> salmonMatches = Arrays.asList(new SalmonMatch(), new SalmonMatch());
        when(salmonMatchService.getAllFromPlayer(anyInt())).thenReturn(salmonMatches);

        mockMvc.perform(get("/api/v1/salmonMatches/player/1")
                .with(user("testUser").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(salmonMatches)));
    }

    @Test
    void testFindById_Positive() throws Exception {
        SalmonMatch salmonMatch = new SalmonMatch();
        when(salmonMatchService.getPartidaSalmon(anyInt())).thenReturn(salmonMatch);

        mockMvc.perform(get("/api/v1/salmonMatches/1")
                .with(user("testUser").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(salmonMatch)));
    }

    @Test
    void testCreate_Positive() throws Exception {
        SalmonMatch salmonMatch = new SalmonMatch();
        when(salmonMatchService.savePartidaSalmon(any(SalmonMatch.class))).thenReturn(salmonMatch);

        mockMvc.perform(post("/api/v1/salmonMatches")
                .with(csrf())
                .with(user("testUser").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(salmonMatch)))
            .andExpect(status().isCreated());
    }

    @Test
    void testUpdateCoordinate_Positive() throws Exception {
        SalmonMatch salmonMatch = new SalmonMatch();
        when(salmonMatchService.getPartidaSalmon(anyInt())).thenReturn(salmonMatch);
        when(salmonMatchService.savePartidaSalmon(any(SalmonMatch.class))).thenReturn(salmonMatch);

        Map<String, Integer> coordinate = Map.of("x", 5, "y", 10);

        mockMvc.perform(patch("/api/v1/salmonMatches/coordinate/1")
                .with(csrf())
                .with(user("testUser").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coordinate)))
            .andExpect(status().isOk());
    }

    @Test
    void testCreatePlayerSalmonMatches_Positive() throws Exception {
        Player p = new Player();
        p.setColor(Color.ROJO);
        p.setId(1);

        mockMvc.perform(post("/api/v1/salmonMatches/player/1")
                .with(csrf())
                .with(user("testUser").roles("USER")))
            .andExpect(status().isOk());
    }
}