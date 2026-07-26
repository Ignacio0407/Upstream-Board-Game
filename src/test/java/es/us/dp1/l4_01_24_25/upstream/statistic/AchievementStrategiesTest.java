package es.us.dp1.l4_01_24_25.upstream.statistic;

import es.us.dp1.l4_01_24_25.upstream.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AchievementStrategiesTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testAUGamesPlayed() {
        AUGamesPlayed strategy = new AUGamesPlayed();
        Achievement achievement = new Achievement();
        achievement.setThreshold(5.0);

        // Null playedgames
        user.setPlayedgames(null);
        assertFalse(strategy.isUnlockedByUser(user, achievement));

        // playedgames < threshold
        user.setPlayedgames(4);
        assertFalse(strategy.isUnlockedByUser(user, achievement));

        // playedgames >= threshold
        user.setPlayedgames(5);
        assertTrue(strategy.isUnlockedByUser(user, achievement));

        user.setPlayedgames(10);
        assertTrue(strategy.isUnlockedByUser(user, achievement));
    }

    @Test
    void testAUTotalPoints() {
        AUTotalPoints strategy = new AUTotalPoints();
        Achievement achievement = new Achievement();
        achievement.setThreshold(50.0);

        // Null totalpoints
        user.setTotalpoints(null);
        assertFalse(strategy.isUnlockedByUser(user, achievement));

        // totalpoints < threshold
        user.setTotalpoints(49);
        assertFalse(strategy.isUnlockedByUser(user, achievement));

        // totalpoints >= threshold
        user.setTotalpoints(50);
        assertTrue(strategy.isUnlockedByUser(user, achievement));

        user.setTotalpoints(100);
        assertTrue(strategy.isUnlockedByUser(user, achievement));
    }

    @Test
    void testAUVictories() {
        AUVictories strategy = new AUVictories();
        Achievement achievement = new Achievement();
        achievement.setThreshold(3.0);

        // Null victories
        user.setVictories(null);
        assertFalse(strategy.isUnlockedByUser(user, achievement));

        // victories < threshold
        user.setVictories(2);
        assertFalse(strategy.isUnlockedByUser(user, achievement));

        // victories >= threshold
        user.setVictories(3);
        assertTrue(strategy.isUnlockedByUser(user, achievement));

        user.setVictories(10);
        assertTrue(strategy.isUnlockedByUser(user, achievement));
    }

    @Test
    void testAchievementUnlocker() {
        AchievementUnlocker unlocker = new AchievementUnlocker();
        
        Achievement achievement = new Achievement();
        achievement.setMetric(Metric.GAMES_PLAYED);
        achievement.setThreshold(5.0);

        user.setPlayedgames(10);
        assertTrue(unlocker.checkUnlock(user, achievement));

        user.setPlayedgames(2);
        assertFalse(unlocker.checkUnlock(user, achievement));

        // Test with unmapped or null strategy metric
        Achievement unknownAchievement = new Achievement();
        unknownAchievement.setMetric(null);
        assertFalse(unlocker.checkUnlock(user, unknownAchievement));
    }
}
