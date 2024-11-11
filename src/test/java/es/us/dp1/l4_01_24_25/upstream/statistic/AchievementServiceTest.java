package es.us.dp1.l4_01_24_25.upstream.statistic;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private AchievementService achievementService;

    private Achievement testAchievement;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testAchievement = new Achievement();
        testAchievement.setId(1);
        testAchievement.setName("First Achievement");
    }

    @Test
    void testFindAchievementById_Success() {
        when(achievementRepository.findById(1)).thenReturn(Optional.of(testAchievement));
        Achievement foundAchievement = achievementService.getById(1);
        assertNotNull(foundAchievement);
        assertEquals("First Achievement", foundAchievement.getName());
    }

    @Test
    void testCreateAchievement() {
        when(achievementRepository.save(any(Achievement.class))).thenReturn(testAchievement);
        Achievement createdAchievement = achievementService.saveAchievement(testAchievement);
        assertNotNull(createdAchievement);
        assertEquals("First Achievement", createdAchievement.getName());
    }

    @Test
    void testDeleteAchievement() {
        when(achievementRepository.findById(1)).thenReturn(Optional.of(testAchievement));
        achievementService.deleteAchievementById(1);
        verify(achievementRepository, times(1)).deleteById(1);
    }
}

