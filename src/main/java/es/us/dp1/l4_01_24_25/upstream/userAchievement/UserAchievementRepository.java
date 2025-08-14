package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.user.User;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Integer>{

    List<UserAchievement> findByUserId(Integer userId);

    UserAchievement findByUserAndAchievement(User u, Achievement a);
}