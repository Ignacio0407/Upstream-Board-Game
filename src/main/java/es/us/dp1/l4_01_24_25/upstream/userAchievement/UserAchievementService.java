package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.model.BaseService;
import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementService;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementUnlocker;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import jakarta.transaction.Transactional;
import es.us.dp1.l4_01_24_25.upstream.user.User;

@Service
public class UserAchievementService extends BaseService<UserAchievement,Integer>{

    UserAchievementRepository userAchievementRepository;
    UserService userService;
    AchievementService achievementService;
    AchievementUnlocker achievementUnlocker = new AchievementUnlocker();

    @Autowired
    public UserAchievementService( UserAchievementRepository userAchievementRepository, @Lazy UserService userService, AchievementService achievementService) {
        super(userAchievementRepository);
        this.userService = userService;
        this.achievementService = achievementService;
    }

    @Transactional
    public UserAchievement findByUserandAchievement(User user, Achievement achievement) throws ResourceNotFoundException {
        if (user == null || achievement == null) {
            throw new ResourceNotFoundException("Usuario o logro no pueden ser nulos.");
        }
        User pu = userService.findById(user.getId());
        Achievement pa = achievementService.findById(achievement.getId());
        return userAchievementRepository.findRepeatedUserAchievement(pu, pa);
    }

    @Transactional
    public void checkAndUnlockAchievements(Integer userId) {
        User user = userService.findById(userId);
        Iterable<Achievement> achievements = achievementService.findAll();
        for(Achievement a:achievements) {
                if(achievementUnlocker.checkUnlock(user, a)) {
                    UserAchievement ua = new UserAchievement(user, a);
                    userAchievementRepository.save(ua);
                }       
        }
    }

    @Transactional
    public UserAchievement unlockRules(@PathVariable("username") String username) throws Exception {
        User u = userService.findUserByName(username);
        Achievement a = achievementService.findById(4);
        UserAchievement ua = new UserAchievement(u, a);
        UserAchievement repeated = this.findByUserandAchievement(u, a);
        if(repeated != null) throw new Exception(); 
        return this.save(ua);
    }
}