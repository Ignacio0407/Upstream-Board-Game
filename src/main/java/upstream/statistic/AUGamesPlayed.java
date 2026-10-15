package upstream.statistic;

import org.springframework.stereotype.Component;

import upstream.user.User;

@Component
public class AUGamesPlayed implements AchievementStrategy{

    @Override
    public boolean isUnlockedByUser(User user, Achievement achievement) {
        return user.getPlayedgames() != null && user.getPlayedgames() >= achievement.getThreshold();
    }

}