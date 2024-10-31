package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface UserAchievementRepository extends CrudRepository<UserAchievement, Long>{

    List<UserAchievement> findByUserId(Integer userId);

}