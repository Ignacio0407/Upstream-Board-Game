package es.us.dp1.l4_01_24_25.upstream.statistic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.configuration.SecurityConfiguration;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;

@WebMvcTest(controllers = AchievementRestController.class, 
excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
class AchievementRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AchievementService achievementService;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindAll_Positive() throws Exception {
        List<Achievement> achievements = Arrays.asList(new Achievement(), new Achievement());
        when(achievementService.findAll()).thenReturn(achievements);
        
        mockMvc.perform(get("/api/v1/achievements"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(achievements)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindAll_Negative() throws Exception {
        when(achievementService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/achievements"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindById_Positive() throws Exception {
        Achievement achievement = new Achievement();
        when(achievementService.findById(1)).thenReturn(achievement);

        mockMvc.perform(get("/api/v1/achievements/1"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(achievement)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindById_Negative() throws Exception {
        when(achievementService.findById(1)).thenReturn(null);

        mockMvc.perform(get("/api/v1/achievements/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindByName_Positive() throws Exception {
        Achievement achievement = new Achievement();
        //when(achievementService.getByName("234")).thenReturn(achievement);

        mockMvc.perform(get("/api/v1/achievements/name/234"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(achievement)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testFindByName_Negative() throws Exception {
        //when(achievementService.getByName("234")).thenReturn(null);

        mockMvc.perform(get("/api/v1/achievements/name/234"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testCreate_Positive() throws Exception {
        Achievement achievement = new Achievement();
        achievement.setName("Test Achievement");
        achievement.setMetric(Metric.EXPLORER);
        when(achievementService.save(any(Achievement.class))).thenReturn(achievement);

        mockMvc.perform(post("/api/v1/achievements")
            .with(csrf())
            .with(user("testUser").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(achievement)))
            .andExpect(status().isCreated())
            .andExpect(content().json(objectMapper.writeValueAsString(achievement)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreate_Negative() throws Exception {
        Achievement invalidAchievement = new Achievement();
        invalidAchievement.setId(1);
        
        mockMvc.perform(post("/api/v1/achievements")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidAchievement)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteById_Positive() throws Exception {
        Achievement achievement = new Achievement();
        when(achievementService.findById(1)).thenReturn(achievement);

        mockMvc.perform(delete("/api/v1/achievements/1")
            .with(csrf())
            .with(user("testUser").roles("ADMIN")))
            .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteById_Negative() throws Exception {
        doThrow(new ResourceNotFoundException("Achievement not found"))
            .when(achievementService).findById(1);

        mockMvc.perform(delete("/api/v1/achievements/1")
            .with(csrf())
            .with(user("testUser").roles("ADMIN")))
            .andExpect(status().isNotFound());
    }

    @Test
    void testUpdate_Positive() throws Exception {
        Achievement achievement = new Achievement();
        achievement.setId(1);
        achievement.setMetric(Metric.EXPLORER);
        when(achievementService.findById(1)).thenReturn(achievement);
        when(achievementService.save(any(Achievement.class))).thenReturn(achievement);

        mockMvc.perform(put("/api/v1/achievements/1")
            .with(csrf()) 
            .with(user("testUser").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(achievement)))
            .andExpect(status().isNoContent());
    }

    @Test
    void testUpdate_Negative() throws Exception {
        doThrow(new ResourceNotFoundException("Achievement not found"))
            .when(achievementService).findById(1);
        Achievement achievement = new Achievement();
        achievement.setId(1);
        achievement.setMetric(Metric.EXPLORER);

        mockMvc.perform(put("/api/v1/achievements/1")
            .with(csrf())
            .with(user("testUser").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(achievement)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser("user")
    void testFindByNames_Positive() throws Exception {
        List<Achievement> achievements = Arrays.asList(new Achievement(), new Achievement());
        //when(achievementService.getByNames(anyList())).thenReturn(achievements);

        mockMvc.perform(get("/api/v1/achievements/names/name1,name2"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(achievements)));
    }

    @Test
    @WithMockUser("user")
    void testFindByNames_Negative() throws Exception {
        //when(achievementService.getByNames(anyList())).thenReturn(null);

        mockMvc.perform(get("/api/v1/achievements/names/name1,name2"))
            .andExpect(status().isNotFound());
    }
}