package es.us.dp1.l4_01_24_25.upstream.statistic;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import es.us.dp1.l4_01_24_25.upstream.user.User;

@Component
public class AchievementUnlocker {

    private Map<Metric, AchievementStrategy> strategies = new EnumMap<>(Metric.class);

    public AchievementUnlocker() {
        strategies.put(Metric.GAMES_PLAYED, new AUGamesPlayed());
        strategies.put(Metric.TOTAL_POINTS, new AUTotalPoints());
        strategies.put(Metric.VICTORIES, new AUVictories());
    }

    public boolean checkUnlock(User user, Achievement achievement) {
        AchievementStrategy strategy = strategies.get(achievement.getMetric());
        if(strategy!=null) return strategy.isUnlockedByUser(user, achievement);
        return false;
    }

}