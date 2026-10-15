package upstream.userachievement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Integer>{

    List<UserAchievement> findByUserId(Integer userId);

    UserAchievement findByUserIdAndAchievementId(Integer userId, Integer achievementId);
}