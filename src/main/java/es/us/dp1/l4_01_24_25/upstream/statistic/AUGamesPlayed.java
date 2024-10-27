package es.us.dp1.l4_01_24_25.upstream.statistic;

import es.us.dp1.l4_01_24_25.upstream.user.User;

public class AUGamesPlayed implements AchievementStrategy{

    @Override
    public boolean isUnlockedByUser(User user, Achievement achievement) {
        return user.getPartidasjugadas() >= achievement.getThreshold();
    }


}