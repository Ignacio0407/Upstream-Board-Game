package es.us.dp1.l4_01_24_25.upstream.statistic;

import org.springframework.stereotype.Component;

import es.us.dp1.l4_01_24_25.upstream.user.User;

@Component
public class AUGamesPlayed implements AchievementStrategy{

    @Override
    public boolean isUnlockedByUser(User user, Achievement achievement) {
        return user.getPlayedgames() != null && user.getPlayedgames() >= achievement.getThreshold();
    }

}