package es.us.dp1.l4_01_24_25.upstream.statistic;

import es.us.dp1.l4_01_24_25.upstream.user.User;

public interface AchievementStrategy {
    boolean isUnlockedByUser(User user, Achievement achievement);
}
