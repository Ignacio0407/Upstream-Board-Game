package upstream.statistic;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import upstream.userachievement.UserAchievementService;

@SpringBootTest
@AutoConfigureMockMvc
class AchievementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Asumo que tu servicio se llama así por el nombre de la variable en tus tests
    @MockBean
    private UserAchievementService userAchievementService; 

    // Ajusta esta ruta si tu controlador base tiene un path distinto
    private static final String BASE_URL = "/api/v1/achievements"; 

    // ── GET /{userId}/achievements ────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "PLAYER")
    void shouldReturnUserAchievements() throws Exception {
        Achievement a = new Achievement();
        a.setId(1);
        a.setName("Expert");

        // Si tu servicio recibe un Integer en lugar de un int primitivo, esto funciona igual.
        // Si usas Long, recuerda poner 1L.
        when(userAchievementService.findUserAchievements(1)).thenReturn(List.of(a));

        mockMvc.perform(get(BASE_URL + "/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Expert"));
    }

    @Test
    @WithMockUser(authorities = "PLAYER")
    void shouldReturnEmptyAchievementsWhenUserHasNone() throws Exception {
        when(userAchievementService.findUserAchievements(1)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    @WithMockUser(authorities = "PLAYER")
    void shouldReturn403WhenPlayerTriesToModifyAchievements() throws Exception {
        // DELETE
        mockMvc.perform(delete("/api/v1/achievements/1").with(csrf()))
                .andExpect(status().isForbidden());

        // POST (No necesitas mandar un body real, el filtro salta antes)
        mockMvc.perform(post("/api/v1/achievements").with(csrf()))
                .andExpect(status().isForbidden());

        // PUT 
        mockMvc.perform(put("/api/v1/achievements/1").with(csrf()))
                .andExpect(status().isForbidden());
    }
}