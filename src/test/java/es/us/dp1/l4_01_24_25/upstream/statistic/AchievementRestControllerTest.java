package es.us.dp1.l4_01_24_25.upstream.statistic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;

class AchievementRestControllerTest {

    @Mock
    private AchievementService achievementService;

    @InjectMocks
    private AchievementRestController achievementRestController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll_Positive() {
        List<Achievement> achievements = Arrays.asList(new Achievement(), new Achievement());
        when(achievementService.getAchievements()).thenReturn(achievements);
        
        ResponseEntity<List<Achievement>> response = achievementRestController.findAll();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(achievements, response.getBody());
    }

    @Test
    void testFindAll_Negative() {
        when(achievementService.getAchievements()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Achievement>> response = achievementRestController.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testFindAchievement_Positive() {
        Achievement achievement = new Achievement();
        when(achievementService.getById(1)).thenReturn(achievement);

        ResponseEntity<Achievement> response = achievementRestController.findById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(achievement, response.getBody());
    }

    @Test
    void testFindAchievement_Negative() {
        when(achievementService.getById(1)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> {
            achievementRestController.findById(1);
        });
    }

    @Test
    void testCreateAchievement_Positive() {
        Achievement achievement = new Achievement();
        when(achievementService.saveAchievement(achievement)).thenReturn(achievement);

        ResponseEntity<Achievement> response = achievementRestController.create(achievement);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(achievement, response.getBody());
    }

    @Test
    void testModifyAchievement_Positive() {
        Achievement existingAchievement = new Achievement();
        Achievement newAchievement = new Achievement();
        newAchievement.setId(1);
        
        when(achievementService.getById(1)).thenReturn(existingAchievement);
        when(achievementService.saveAchievement(existingAchievement)).thenReturn(existingAchievement);

        ResponseEntity<Void> response = achievementRestController.modifyAchievement(newAchievement, 1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(achievementService, times(1)).saveAchievement(existingAchievement);
    }

    @Test
    void testModifyAchievement_Negative_IdMismatch() {
        Achievement newAchievement = new Achievement();
        newAchievement.setId(2); // Different from the path variable id (1)

        // Es necesario para que no me salte la ResourceNotFoundException del controller.
        when(achievementService.getById(1)).thenReturn(new Achievement());

        ResponseEntity<Void> response = achievementRestController.modifyAchievement(newAchievement, 1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteAchievement_Positive() {
        Achievement achievement = new Achievement();
        when(achievementService.getById(1)).thenReturn(achievement);

        ResponseEntity<Void> response = achievementRestController.deleteAchievement(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(achievementService, times(1)).deleteAchievementById(1);
    }

    @Test
    void testDeleteAchievement_Negative() {
        when(achievementService.getById(1)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> {
            achievementRestController.deleteAchievement(1);
        });
    }
}