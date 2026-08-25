package es.us.dp1.l4_01_24_25.upstream.statistic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private AchievementService achievementService;

    @Test
    void shouldUpdateAllAchievementFields() {
        Achievement existing = new Achievement();
        existing.setId(1);
        existing.setName("Old Name");
        existing.setDescription("Old Description");
        existing.setThreshold(10.0);
        existing.setMetric(Metric.VICTORIES);
        existing.setBadgeImage("old_badge.png");

        Achievement updated = new Achievement();
        updated.setName("New Name");
        updated.setDescription("New Description");
        updated.setThreshold(20.0);
        updated.setMetric(Metric.TOTAL_POINTS);
        updated.setBadgeImage("new_badge.png");

        when(achievementRepository.findById(1)).thenReturn(Optional.of(existing));
        when(achievementRepository.save(existing)).thenReturn(existing);

        Achievement result = achievementService.update(1, updated);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getThreshold()).isEqualTo(20.0);
        assertThat(result.getMetric()).isEqualTo(Metric.TOTAL_POINTS);
        assertThat(result.getBadgeImage()).isEqualTo("new_badge.png");
        verify(achievementRepository).save(existing);
    }

    @Test
    void shouldUpdateAchievementWithNullBadgeImage() {
        Achievement existing = new Achievement();
        existing.setId(1);
        existing.setBadgeImage("old_badge.png");

        Achievement updated = new Achievement();
        updated.setName("Name");
        updated.setDescription("Desc");
        updated.setThreshold(5.0);
        updated.setMetric(Metric.VICTORIES);
        updated.setBadgeImage(null);

        when(achievementRepository.findById(1)).thenReturn(Optional.of(existing));
        when(achievementRepository.save(existing)).thenReturn(existing);

        Achievement result = achievementService.update(1, updated);

        assertThat(result.getBadgeImage()).isNull();
    }
}
