package upstream.salmonmatch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import upstream.exceptions.InsufficientEnergyException;
import upstream.exceptions.ResourceNotFoundException;
import upstream.salmonmatch.dto.SalmonMatchDTO;

@WebMvcTest(controllers = SalmonMatchController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class))
@AutoConfigureMockMvc(addFilters = false)
class SalmonMatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SalmonMatchService salmonMatchService;

    // ── GET /match/{matchId} ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /match/{matchId} - Happy Path")
    void shouldFindAllFromMatch() throws Exception {
        SalmonMatchDTO dto = new SalmonMatchDTO();
        dto.setId(1);
        when(salmonMatchService.findAllFromMatchDTO(1)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/salmonMatches/match/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1));

        verify(salmonMatchService).findAllFromMatchDTO(1);
    }

    @Test
    @DisplayName("GET /match/{matchId} - Edge Case (Type Mismatch -> 400)")
    void shouldFailFindAllFromMatchOnTypeMismatch() throws Exception {
        mockMvc.perform(get("/api/v1/salmonMatches/match/abc"))
               .andExpect(status().isBadRequest());

        verify(salmonMatchService, never()).findAllFromMatchDTO(any());
    }

    // ── PATCH /move-batch/{id} ────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /move-batch/{id} - Happy Path")
    void shouldUpdateCoordinateBatch() throws Exception {
        String payload = "[{\"salmonId\": 1, \"x\": 1, \"y\": 2, \"enterSpawn\": false}]";
        doNothing().when(salmonMatchService).updateCoordinateBatch(eq(1), any());

        mockMvc.perform(patch("/api/v1/salmonMatches/move-batch/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(payload))
               .andExpect(status().isOk());

        verify(salmonMatchService).updateCoordinateBatch(eq(1), any());
    }

    @Test
    @DisplayName("PATCH /move-batch/{id} - Negative Path (Insufficient Energy -> 400)")
    void shouldFailUpdateCoordinateBatchNoEnergy() throws Exception {
        String payload = "[{\"salmonId\": 1, \"x\": 1, \"y\": 2, \"enterSpawn\": false}]";
        doThrow(new InsufficientEnergyException("No energy"))
            .when(salmonMatchService).updateCoordinateBatch(eq(1), any());

        mockMvc.perform(patch("/api/v1/salmonMatches/move-batch/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(payload))
               .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /move-batch/{id} - Edge Case (Malformed JSON -> 400)")
    void shouldFailUpdateCoordinateBatchOnMalformedJson() throws Exception {
        mockMvc.perform(patch("/api/v1/salmonMatches/move-batch/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content("[{ \"salmonId\": 1, \"x\" }]")) // Array JSON Roto
               .andExpect(status().isBadRequest());

        verify(salmonMatchService, never()).updateCoordinateBatch(any(), any());
    }

    // ── POST /player/{playerId} ───────────────────────────────────────────────

    @Test
    @DisplayName("POST /player/{playerId} - Happy Path")
    void shouldCreateSalmonMatchesForPlayer() throws Exception {
        mockMvc.perform(post("/api/v1/salmonMatches/player/1"))
            .andExpect(status().isOk());

        verify(salmonMatchService).create(1);
    }

    @Test
    @DisplayName("POST /player/{playerId} - Negative Path (Player Not Found -> 404)")
    void shouldFailCreateSalmonMatchesWhenPlayerNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Player not found"))
            .when(salmonMatchService).create(99);

        mockMvc.perform(post("/api/v1/salmonMatches/player/99"))
            .andExpect(status().isNotFound());
    }
}