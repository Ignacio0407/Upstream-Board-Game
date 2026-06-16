package es.us.dp1.l4_01_24_25.upstream.statistic;

import java.util.HashMap;
import java.util.Map;

import es.us.dp1.l4_01_24_25.upstream.user.User;

public class AchievementUnlocker {

    private Map<Metric, AchievementStrategy> estrategias = new HashMap<>();

    public AchievementUnlocker() {
        estrategias.put(Metric.GAMES_PLAYED, new AUGamesPlayed());
        estrategias.put(Metric.TOTAL_POINTS, new AUTotalPoints());
        estrategias.put(Metric.VICTORIES, new AUVictories());
    }

    public boolean checkUnlock(User user, Achievement achievement) {
        AchievementStrategy estrategia = estrategias.get(achievement.getMetric());
        if(estrategia!=null) return estrategia.isUnlockedByUser(user, achievement);
        return false;
    }

}