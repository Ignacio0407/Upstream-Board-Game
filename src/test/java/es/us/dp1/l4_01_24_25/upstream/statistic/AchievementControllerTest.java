package es.us.dp1.l4_01_24_25.upstream.statistic;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.configuration.SecurityConfiguration;
import es.us.dp1.l4_01_24_25.upstream.exceptions.BadRequestException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;

@WebMvcTest(controllers = AchievementRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
class AchievementRestControllerTests {

    private static final int TEST_ACHIEVEMENT_ID = 1;
    private static final String BASE_URL = "/api/v1/achievements";

    @Autowired
    private AchievementRestController achievementController;

    @MockBean
    private AchievementService achievementService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private Achievement achievement;

    @BeforeEach
    void setup() {
        achievement = new Achievement();
        achievement.setId(TEST_ACHIEVEMENT_ID);
        achievement.setName("Test Achievement");
        achievement.setDescription("This is a test achievement");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFindAllAchievements() throws Exception {
        when(this.achievementService.getAchievements()).thenReturn(List.of(achievement));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(TEST_ACHIEVEMENT_ID))
                .andExpect(jsonPath("$[0].name").value("Test Achievement"))
                .andExpect(jsonPath("$[0].description").value("This is a test achievement"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFindAchievement() throws Exception {
        when(this.achievementService.getById(TEST_ACHIEVEMENT_ID)).thenReturn(achievement);

        mockMvc.perform(get(BASE_URL + "/{id}", TEST_ACHIEVEMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_ACHIEVEMENT_ID))
                .andExpect(jsonPath("$.name").value("Test Achievement"))
                .andExpect(jsonPath("$.description").value("This is a test achievement"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundAchievement() throws Exception {
        when(this.achievementService.getById(TEST_ACHIEVEMENT_ID)).thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get(BASE_URL + "/{id}", TEST_ACHIEVEMENT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAchievement() throws Exception {
        Achievement newAchievement = new Achievement();
        newAchievement.setName("New Achievement");
        newAchievement.setDescription("This is a new achievement");

        when(this.achievementService.saveAchievement(newAchievement)).thenReturn(achievement);

        mockMvc.perform(post(BASE_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAchievement)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_ACHIEVEMENT_ID))
                .andExpect(jsonPath("$.name").value("Test Achievement"))
                .andExpect(jsonPath("$.description").value("This is a test achievement"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestOnAchievementCreation() throws Exception {
        Achievement newAchievement = new Achievement();
        newAchievement.setDescription("This is a new achievement");

        when(this.achievementService.saveAchievement(newAchievement)).thenThrow(BadRequestException.class);

        mockMvc.perform(post(BASE_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAchievement)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAchievement() throws Exception {
        Achievement updatedAchievement = new Achievement();
        updatedAchievement.setId(TEST_ACHIEVEMENT_ID);
        updatedAchievement.setName("Updated Achievement");
        updatedAchievement.setDescription("This is an updated achievement");

        when(this.achievementService.getById(TEST_ACHIEVEMENT_ID)).thenReturn(achievement);
        when(this.achievementService.saveAchievement(updatedAchievement)).thenReturn(updatedAchievement);

        mockMvc.perform(put(BASE_URL + "/{id}", TEST_ACHIEVEMENT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedAchievement)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundOnAchievementUpdate() throws Exception {
        Achievement updatedAchievement = new Achievement();
        updatedAchievement.setId(TEST_ACHIEVEMENT_ID);
        updatedAchievement.setName("Updated Achievement");
        updatedAchievement.setDescription("This is an updated achievement");

        when(this.achievementService.getById(TEST_ACHIEVEMENT_ID)).thenThrow(ResourceNotFoundException.class);
        when(this.achievementService.saveAchievement(updatedAchievement)).thenReturn(updatedAchievement);

        mockMvc.perform(put(BASE_URL + "/{id}", TEST_ACHIEVEMENT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedAchievement)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteAchievement() throws Exception {
        when(this.achievementService.getById(TEST_ACHIEVEMENT_ID)).thenReturn(achievement);
        doNothing().when(this.achievementService).deleteAchievementById(TEST_ACHIEVEMENT_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", TEST_ACHIEVEMENT_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
}