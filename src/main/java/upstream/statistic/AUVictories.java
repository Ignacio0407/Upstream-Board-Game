package upstream.statistic;

import upstream.user.User;

public class AUVictories implements AchievementStrategy{

    @Override
    public boolean isUnlockedByUser(User user, Achievement achievement) {
        return user.getVictories() != null && user.getVictories() >= achievement.getThreshold();
    }
    
}
