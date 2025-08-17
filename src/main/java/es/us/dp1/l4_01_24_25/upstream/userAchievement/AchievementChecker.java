package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import org.springframework.stereotype.Component;

import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Component
public class AchievementChecker {
    
    private final UserAchievementService userAchievementService;
    private final UserRepository userRepository;

    public AchievementChecker(UserAchievementService userAchievementService, UserRepository userRepository) {
        this.userAchievementService = userAchievementService;
        this.userRepository = userRepository;
    }

    @PostConstruct
    @Transactional
    public void checkAchievements() {
        Iterable<User> users = this.userRepository.findAll();
        for(User u:users) {
            this.userAchievementService.checkAndUnlockAchievements(u.getId());
        }

    }

}