package upstream.statistic;

import upstream.user.User;

public class AUTotalPoints implements AchievementStrategy{

    @Override
    public boolean isUnlockedByUser(User user, Achievement achievement) {
        return user.getTotalpoints() != null && user.getTotalpoints() >= achievement.getThreshold();
    }
    
}
