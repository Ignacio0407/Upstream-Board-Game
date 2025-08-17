package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Integer>{

    List<UserAchievement> findByUserId(Integer userId);

    UserAchievement findByUserIdAndAchievementId(Integer userId, Integer achievementId);
}