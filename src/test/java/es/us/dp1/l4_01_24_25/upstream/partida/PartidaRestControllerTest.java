package es.us.dp1.l4_01_24_25.upstream.partida;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.configuration.SecurityConfiguration;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;

@WebMvcTest(controllers = PartidaRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
class PartidaRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartidaService partidaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Partida partida1;
    private Partida partida2;

    @BeforeEach
    void setup() {
        partida1 = new Partida();
        partida1.setId(1);
        partida1.setName("Partida1");

        partida2 = new Partida();
        partida2.setId(2);
        partida2.setName("Partida2");
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testFindAllPartidas() throws Exception {
        List<Partida> partidas = Arrays.asList(partida1, partida2);
        when(partidaService.getPartidas()).thenReturn(partidas);

        mockMvc.perform(get("/api/v1/matches"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Partida1"))
            .andExpect(jsonPath("$[1].name").value("Partida2"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testFindSomePartidasById_Success() throws Exception {
        List<Partida> partidas = Arrays.asList(partida1, partida2);
        when(partidaService.getSomePartidasById(Arrays.asList(1, 2))).thenReturn(partidas);

        mockMvc.perform(get("/api/v1/matches/ids/{ids}", "1,2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Partida1"))
            .andExpect(jsonPath("$[1].name").value("Partida2"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testFindSomePartidasById_NotFound() throws Exception {
        when(partidaService.getSomePartidasById(any()))
            .thenThrow(new ResourceNotFoundException("Partidas no encontradas"));

        mockMvc.perform(get("/api/v1/matches/ids/{ids}", "1,2"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testFindSomePartidasByName_Success() throws Exception {
        List<Partida> partidas = Arrays.asList(partida1, partida2);
        when(partidaService.getSomePartidasByName(Arrays.asList("Partida1", "Partida2"))).thenReturn(partidas);

        mockMvc.perform(get("/api/v1/matches/names/{names}", "Partida1,Partida2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Partida1"))
            .andExpect(jsonPath("$[1].name").value("Partida2"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testFindSomePartidasByName_NotFound() throws Exception {
        when(partidaService.getSomePartidasByName(any()))
            .thenThrow(new ResourceNotFoundException("Partidas no encontradas"));

        mockMvc.perform(get("/api/v1/matches/names/{names}", "Partida1,Partida2"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testFindPartidaById_Success() throws Exception {
        when(partidaService.getPartidaById(1)).thenReturn(partida1);

        mockMvc.perform(get("/api/v1/matches/{id}", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Partida1"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testFindPartidaById_NotFound() throws Exception {
        when(partidaService.getPartidaById(any()))
            .thenThrow(new ResourceNotFoundException("Partida no encontrada"));

        mockMvc.perform(get("/api/v1/matches/{id}", 1))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testFindPartidaByName_Success() throws Exception {
        when(partidaService.getPartidaByName("Partida1")).thenReturn(partida1);

        mockMvc.perform(get("/api/v1/matches/name/{name}", "Partida1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testFindPartidaByName_NotFound() throws Exception {
        when(partidaService.getPartidaByName(any()))
            .thenThrow(new ResourceNotFoundException("Partida no encontrada"));

        mockMvc.perform(get("/api/v1/matches/name/{name}", "NonExistent"))
            .andExpect(status().isNotFound());
    }

    @Test
    //@WithMockUser(username = "testUser", roles = {"ADMIN"})
    void testDeleteAllPartidas() throws Exception {
        mockMvc.perform(delete("/api/v1/matches"))
            .andExpect(status().isOk());

        verify(partidaService).deleteAllPartidas();
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"ADMIN"})
    void testDeleteSomePartidasById_Success() throws Exception {
        when(partidaService.getPartidaById(any())).thenReturn(partida1);

        mockMvc.perform(delete("/api/v1/matches/ids/{ids}", "1,2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Partidas borradas"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testDeleteSomePartidasById_NotFound() throws Exception {
        when(partidaService.getPartidaById(any()))
            .thenThrow(new ResourceNotFoundException("Partida no encontrada"));

        mockMvc.perform(delete("/api/v1/matches/ids/{ids}", "1,2"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testDeletePartidaById_Success() throws Exception {
        when(partidaService.getPartidaById(1)).thenReturn(partida1);

        mockMvc.perform(delete("/api/v1/matches/{id}", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Partida borrada"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testDeletePartidaById_NotFound() throws Exception {
        when(partidaService.getPartidaById(any()))
            .thenThrow(new ResourceNotFoundException("Partida no encontrada"));

        mockMvc.perform(delete("/api/v1/matches/{id}", 1))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testDeletePartidaByName_Success() throws Exception {
        when(partidaService.getPartidaByName("Partida1")).thenReturn(partida1);

        mockMvc.perform(delete("/api/v1/matches/name/{name}", "Partida1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Partida borrada"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testDeletePartidaByName_NotFound() throws Exception {
        when(partidaService.getPartidaByName(any()))
            .thenThrow(new ResourceNotFoundException("Partida no encontrada"));

        mockMvc.perform(delete("/api/v1/matches/name/{name}", "NonExistent"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testUpdatePartidaById_Success() throws Exception {
        when(partidaService.getPartidaById(1)).thenReturn(partida1);
        when(partidaService.updatePartidaById(any(), eq(1))).thenReturn(partida1);

        mockMvc.perform(put("/api/v1/matches/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(partida1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Partida1"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testUpdatePartidaById_NotFound() throws Exception {
        when(partidaService.getPartidaById(any()))
            .thenThrow(new ResourceNotFoundException("Partida no encontrada"));

        mockMvc.perform(put("/api/v1/matches/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(partida1)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testUpdatePartidaByName_Success() throws Exception {
        when(partidaService.getPartidaByName("Partida1")).thenReturn(partida1);
        when(partidaService.updatePartidaByName(any(), eq("Partida1"))).thenReturn(partida1);

        mockMvc.perform(put("/api/v1/matches/name/{name}", "Partida1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(partida1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Partida1"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testUpdatePartidaByName_NotFound() throws Exception {
        when(partidaService.getPartidaByName(any()))
            .thenThrow(new ResourceNotFoundException("Partida no encontrada"));

        mockMvc.perform(put("/api/v1/matches/name/{name}", "NonExistent")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(partida1)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testCreatePartida_Success() throws Exception {
        when(partidaService.savePartida(any())).thenReturn(partida1);

        mockMvc.perform(post("/api/v1/matches")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(partida1)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Partida1"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testCreatePartida_BadRequest() throws Exception {
        Partida invalidPartida = new Partida(); // Sin campos requeridos

        mockMvc.perform(post("/api/v1/matches")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidPartida)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testCreatePartida_MissingRequiredFields() throws Exception {
        Partida invalidPartida = new Partida();
        // Don't set any fields to trigger validation errors

        mockMvc.perform(post("/api/v1/matches")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidPartida)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testUpdatePartidaById_InvalidData() throws Exception {
        Partida invalidPartida = new Partida();
        // Don't set any fields to trigger validation errors

        mockMvc.perform(put("/api/v1/matches/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidPartida)))
            .andExpect(status().isBadRequest());
    }

    // Test for partial success in batch operations
        @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testDeleteSomePartidasByIdPartialSuccess() throws Exception {
        // Given
        when(partidaService.getPartidaById(1)).thenReturn(partida1);
        when(partidaService.getPartidaById(2))
            .thenThrow(new ResourceNotFoundException("Partida 2 no encontrada"));
        String ids = "1,2";

        // When & Then
        mockMvc.perform(delete("/api/v1/matches/ids/{ids}", ids))
            .andExpect(status().isNotFound());

        verify(partidaService).getPartidaById(1);
        verify(partidaService).getPartidaById(2);
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testFindSomePartidasByIdEmptyList() throws Exception {
        // Given
        List<Integer> emptyList = Arrays.asList();
        when(partidaService.getSomePartidasById(emptyList))
            .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/matches/ids/{ids}", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        verify(partidaService).getSomePartidasById(emptyList);
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testDeleteSomePartidasByIdEmptyList() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/matches/ids/{ids}", ""))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testFindSomePartidasByIdInvalidFormat() throws Exception {
        // Given
        String invalidIds = "1,abc";

        // When & Then
        mockMvc.perform(get("/api/v1/matches/ids/{ids}", invalidIds))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testUpdatePartidaByIdInvalidId() throws Exception {
        // Given
        String invalidId = "abc";

        // When & Then
        mockMvc.perform(put("/api/v1/matches/{id}", invalidId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(partida1)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testCreatePartidaNullBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/matches")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testUpdatePartidaByIdNullBody() throws Exception {
        // Given
        Integer validId = 1;

        // When & Then
        mockMvc.perform(put("/api/v1/matches/{id}", validId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}