package es.us.dp1.l4_01_24_25.upstream.statistic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    private AchievementService achievementService;

    @BeforeEach
    void setUp() {
        achievementService = new AchievementService(achievementRepository);
    }

    @Test
    @DisplayName("update - Should correctly update all Achievement fields")
    void update_AchievementFields_CorrectlyMapped() {
        // Arrange
        Integer achievementId = 1;
        Achievement existing = new Achievement();
        existing.setId(achievementId);
        existing.setName("Old Name");
        existing.setDescription("Old Description");
        existing.setThreshold(10.0);
        existing.setMetric(Metric.VICTORIES);
        existing.setBadgeImage("old_badge.png");

        Achievement updatedInfo = new Achievement();
        updatedInfo.setName("New Name");
        updatedInfo.setDescription("New Description");
        updatedInfo.setThreshold(20.0);
        updatedInfo.setMetric(Metric.TOTAL_POINTS);
        updatedInfo.setBadgeImage("new_badge.png");

        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(existing));
        when(achievementRepository.save(existing)).thenReturn(existing);

        // Act
        Achievement result = achievementService.update(achievementId, updatedInfo);

        // Assert
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getThreshold()).isEqualTo(20.0);
        assertThat(result.getMetric()).isEqualTo(Metric.TOTAL_POINTS);
        assertThat(result.getBadgeImage()).isEqualTo("new_badge.png");
        
        verify(achievementRepository).save(existing);
    }
}
