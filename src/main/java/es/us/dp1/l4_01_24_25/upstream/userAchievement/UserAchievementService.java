package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementRepository;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementUnlocker;
import es.us.dp1.l4_01_24_25.upstream.user.UserRepository;
import jakarta.transaction.Transactional;
import es.us.dp1.l4_01_24_25.upstream.user.User;

@Service
public class UserAchievementService {

    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private AchievementUnlocker achievementUnlocker = new AchievementUnlocker();

    @Autowired
    public UserAchievementService(UserRepository userRepository, AchievementRepository achievementRepository, UserAchievementRepository userAchievementRepository) {
        this.userRepository = userRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.achievementRepository = achievementRepository;
    }

    @Transactional
    public List<UserAchievement> findAll() {
        return userAchievementRepository.findAll();
    }

    @Transactional
    public UserAchievement saveUA(UserAchievement ua) throws DataAccessException {
        userAchievementRepository.save(ua);
        return ua;
    }

    @Transactional
    public Optional<UserAchievement> findById(Integer id) {
        return userAchievementRepository.findById(id);
    }

    @Transactional
    public UserAchievement findByUandA(User u, Achievement a) {
        return userAchievementRepository.findRepeatedUserAchievement(u, a);
    }

    @Transactional
    public void checkAndUnlockAchievements(Integer userId) {
        User user = userRepository.findById(userId).get();
        Iterable<Achievement> achievements = achievementRepository.findAll();
        for(Achievement a:achievements) {
                if(achievementUnlocker.checkUnlock(user, a)) {
                    UserAchievement ua = new UserAchievement(user, a);
                    userAchievementRepository.save(ua);
                }
                
        }

        
    }

}