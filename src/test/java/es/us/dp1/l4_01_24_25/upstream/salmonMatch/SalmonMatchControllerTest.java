package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

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

import es.us.dp1.l4_01_24_25.upstream.exceptions.InsufficientEnergyException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SalmonMatchDTO;

@WebMvcTest(controllers = SalmonMatchController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class))
@AutoConfigureMockMvc(addFilters = false)
class SalmonMatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Solo necesitamos el servicio del controlador
    @MockBean
    private SalmonMatchService salmonMatchService;

    // ==========================================
    // GET /api/v1/salmonMatches/match/{matchId}
    // ==========================================

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

    // ==========================================
    // GET /api/v1/salmonMatches/match/{matchId}/spawn
    // ==========================================

    @Test
    @DisplayName("GET /match/{matchId}/spawn - Happy Path")
    void shouldFindAllFromMatchInSpawn() throws Exception {
        SalmonMatchDTO dto = new SalmonMatchDTO();
        dto.setId(2);
        when(salmonMatchService.findFromGameInSpawnDTO(1)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/salmonMatches/match/1/spawn"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(2));

        verify(salmonMatchService).findFromGameInSpawnDTO(1);
    }

    // ==========================================
    // GET /api/v1/salmonMatches/player/{playerId}
    // ==========================================

    @Test
    @DisplayName("GET /player/{playerId} - Happy Path")
    void shouldFindAllFromPlayer() throws Exception {
        SalmonMatchDTO dto = new SalmonMatchDTO();
        dto.setId(3);
        when(salmonMatchService.findAllFromPlayerDTO(1)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/salmonMatches/player/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(3));

        verify(salmonMatchService).findAllFromPlayerDTO(1);
    }

    @Test
    @DisplayName("GET /player/{playerId} - Edge Case (Type Mismatch -> 400)")
    void shouldFailFindAllFromPlayerOnTypeMismatch() throws Exception {
        mockMvc.perform(get("/api/v1/salmonMatches/player/invalid"))
               .andExpect(status().isBadRequest());

        verify(salmonMatchService, never()).findAllFromPlayerDTO(any());
    }

    // ==========================================
    // PATCH /api/v1/salmonMatches/coordinate/{id}
    // ==========================================

    @Test
    @DisplayName("PATCH /coordinate/{id} - Happy Path")
    void shouldUpdateCoordinate() throws Exception {
        Map<String, Integer> newCoord = Map.of("x", 1, "y", 0);
        doNothing().when(salmonMatchService).updateCoordinate(eq(1), any());

        mockMvc.perform(patch("/api/v1/salmonMatches/coordinate/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(newCoord)))
               .andExpect(status().isOk());

        verify(salmonMatchService).updateCoordinate(eq(1), any());
    }

    @Test
    @DisplayName("PATCH /coordinate/{id} - Negative Path (Insufficient Energy -> 400)")
    void shouldFailUpdateCoordinateNoEnergy() throws Exception {
        Map<String, Integer> newCoord = Map.of("x", 1, "y", 0);
        doThrow(new InsufficientEnergyException("No energy"))
            .when(salmonMatchService).updateCoordinate(eq(1), any());

        mockMvc.perform(patch("/api/v1/salmonMatches/coordinate/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(newCoord)))
               .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /coordinate/{id} - Edge Case (Malformed JSON -> 400)")
    void shouldFailUpdateCoordinateOnMalformedJson() throws Exception {
        mockMvc.perform(patch("/api/v1/salmonMatches/coordinate/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{ \"x\": 1, \"y\" }")) // JSON Roto
               .andExpect(status().isBadRequest());

        verify(salmonMatchService, never()).updateCoordinate(any(), any());
    }

    // ==========================================
    // POST /api/v1/salmonMatches/player/{playerId}
    // ==========================================

    @Test
    @DisplayName("POST /player/{playerId} - Happy Path")
    void shouldCreateSalmonMatchesForPlayer() throws Exception {
        SalmonMatchDTO dto = new SalmonMatchDTO();
        dto.setId(1);
        when(salmonMatchService.create(1)).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/v1/salmonMatches/player/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1));

        verify(salmonMatchService).create(1);
    }

    @Test
    @DisplayName("POST /player/{playerId} - Negative Path (Player Not Found -> 404)")
    void shouldFailCreateSalmonMatchesWhenPlayerNotFound() throws Exception {
        when(salmonMatchService.create(99))
            .thenThrow(new ResourceNotFoundException("Player not found"));

        mockMvc.perform(post("/api/v1/salmonMatches/player/99"))
               .andExpect(status().isNotFound());
    }

    // ==========================================
    // PATCH /api/v1/salmonMatches/enterSpawn/{id}
    // ==========================================

    @Test
    @DisplayName("PATCH /enterSpawn/{id} - Happy Path")
    void shouldHandleEnterSpawn() throws Exception {
        doNothing().when(salmonMatchService).enterSpawn(1);

        mockMvc.perform(patch("/api/v1/salmonMatches/enterSpawn/1"))
               .andExpect(status().isOk());

        verify(salmonMatchService).enterSpawn(1);
    }

    @Test
    @DisplayName("PATCH /enterSpawn/{id} - Negative Path (Not Valid Move -> 400)")
    void shouldFailEnterSpawnWhenNotRound6() throws Exception {
        doThrow(new NotValidMoveException("You can only enter the spawn from round 6!"))
            .when(salmonMatchService).enterSpawn(1);

        mockMvc.perform(patch("/api/v1/salmonMatches/enterSpawn/1"))
               .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /enterSpawn/{id} - Edge Case (Type Mismatch -> 400)")
    void shouldFailEnterSpawnOnTypeMismatch() throws Exception {
        mockMvc.perform(patch("/api/v1/salmonMatches/enterSpawn/abc"))
               .andExpect(status().isBadRequest());

        verify(salmonMatchService, never()).enterSpawn(any());
    }
}