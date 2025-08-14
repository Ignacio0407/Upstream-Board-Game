package es.us.dp1.l4_01_24_25.upstream.userachievement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievement;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementRepository;

@ExtendWith(MockitoExtension.class)
public class UserAchievementRepositoryTest {

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Test
    public void testFindRepeatedUserAchievement() {
        User user = new User();
        user.setId(1);
        Achievement achievement = new Achievement();
        achievement.setId(1);

        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUser(user);
        userAchievement.setAchievement(achievement);

        when(userAchievementRepository.findByUserAndAchievement(user, achievement)).thenReturn(userAchievement);

        UserAchievement result = userAchievementRepository.findByUserAndAchievement(user, achievement);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(achievement, result.getAchievement());

        User invalidUser = new User();
        invalidUser.setId(2);

        when(userAchievementRepository.findByUserAndAchievement(invalidUser, achievement)).thenReturn(null);

        result = userAchievementRepository.findByUserAndAchievement(invalidUser, achievement);

        assertNull(result);
    }
}