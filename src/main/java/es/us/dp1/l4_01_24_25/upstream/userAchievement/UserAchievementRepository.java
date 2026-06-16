package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.user.User;

public interface UserAchievementRepository extends CrudRepository<UserAchievement, Long>{

    List<UserAchievement> findByUserId(Integer userId);

    Optional<UserAchievement> findById(Integer id);

    @SuppressWarnings("override")
    List<UserAchievement> findAll();

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :u AND ua.achievement = :a")
    UserAchievement findRepeatedUserAchievement(User u, Achievement a);

}