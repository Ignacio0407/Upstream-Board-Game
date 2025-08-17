package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import es.us.dp1.l4_01_24_25.upstream.match.matchDTO.MatchDTO;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchRestController.class)
@AutoConfigureMockMvc
class MatchRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchService matchService;

    @Test
    void updateRound_shouldReturn200AndUpdatedMatchDTO() throws Exception {
        MatchDTO dto = new MatchDTO();
        dto.setRound(3);
        when(matchService.updateRound(1)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/matches/1/ronda"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.round").value(3));
    }

    @Test
    void createMatchWMatchCreator_shouldReturn201() throws Exception {
        MatchDTO dto = new MatchDTO();
        dto.setName("Test Match");

        when(matchService.createMatchWMatchCreator(eq(1), any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/matches/matchCreator/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test Match\", \"password\": \"123\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Test Match"));
    }

    @Test
    void startGame_shouldReturn200() throws Exception {
        MatchDTO dto = new MatchDTO();
        dto.setId(1);
        when(matchService.startGame(1)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/matches/1/startGame"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void finalScore_shouldReturn200() throws Exception {
        MatchDTO dto = new MatchDTO();
        dto.setFinalScoreCalculated(true);

        when(matchService.finalScore(1)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/matches/finalscore/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.finalScoreCalculated").value(true));
    }

    @ParameterizedTest
    @CsvSource({"0", "1", "2"})
    void baseController_findById_shouldReturnDTO(Integer id) throws Exception {
        MatchDTO dto = new MatchDTO();
        dto.setId(id);
        when(matchService.findByIdAsDTO(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/matches/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void baseController_findAll_shouldReturnList() throws Exception {
        MatchDTO dto = new MatchDTO();
        dto.setId(1);
        when(matchService.findAllAsDTO()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/matches"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void baseController_save_shouldReturnSavedDTO() throws Exception {
        MatchDTO input = new MatchDTO();
        input.setName("Match X");

        MatchDTO saved = new MatchDTO();
        saved.setName("Match X");

        when(matchService.saveAsDTO(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Match X\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Match X"));
    }

    @Test
    void baseController_update_shouldReturnUpdatedDTO() throws Exception {
        MatchDTO updated = new MatchDTO();
        updated.setId(1);
        updated.setName("Updated Match");

        when(matchService.updateAsDTO(any(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"name\":\"Updated Match\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Match"));
    }

    @Test
    void baseController_delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/matches/1"))
            .andExpect(status().isNoContent());

        verify(matchService).delete(1);
    }
}