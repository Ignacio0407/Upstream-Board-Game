package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import java.util.ArrayList;
import java.util.List;

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
            throw new ResourceNotFoundException("Neither user nor achievement can be null.");
        }
        return this.userAchievementRepository.findByUserIdAndAchievementId(user.getId(), achievement.getId());
    }

    @Transactional
    public void checkAndUnlockAchievements(Integer userId) {
        User user = this.userService.findById(userId);
        Iterable<Achievement> achievements = this.achievementService.findAll();
        List<UserAchievement> toSave = new ArrayList<>();
        for(Achievement a:achievements) {
                if(this.achievementUnlocker.checkUnlock(user, a)) {
                    UserAchievement ua = new UserAchievement(user, a);
                    toSave.add(ua);
                }       
        }
        this.saveAll(toSave);
    }

    @Transactional
    public UserAchievement unlockRules(@PathVariable("username") String username) throws Exception {
        User u = this.userService.findUserByName(username);
        Achievement a = this.achievementService.findById(4);
        UserAchievement ua = new UserAchievement(u, a);
        UserAchievement repeated = this.findByUserandAchievement(u, a);
        if(repeated != null) throw new Exception(); 
        return this.save(ua);
    }
}