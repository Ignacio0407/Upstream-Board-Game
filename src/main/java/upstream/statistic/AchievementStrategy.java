package upstream.statistic;

import upstream.user.User;

public interface AchievementStrategy {
    boolean isUnlockedByUser(User user, Achievement achievement);
}
