package es.us.dp1.l4_01_24_25.upstream.userachievement;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.model.BaseService;
import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementService;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementUnlocker;
import org.springframework.transaction.annotation.Transactional;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@Service
public class UserAchievementService extends BaseService<UserAchievement,Integer>{

    UserAchievementRepository userAchievementRepository;
    UserService userService;
    AchievementService achievementService;
    private final AchievementUnlocker achievementUnlocker;

    @Autowired
    public UserAchievementService( UserAchievementRepository userAchievementRepository, @Lazy UserService userService, @Lazy AchievementService achievementService, AchievementUnlocker achievementUnlocker) {
        super(userAchievementRepository);
        this.userAchievementRepository = userAchievementRepository;
        this.userService = userService;
        this.achievementService = achievementService;
        this.achievementUnlocker = achievementUnlocker;
    }

    public List<UserAchievement> findByUserId(Integer userId) throws ResourceNotFoundException {
        if (userId == null) {
            throw new ResourceNotFoundException("User ID cannot be null.");
        }
        return this.userAchievementRepository.findByUserId(userId);
    }

    public UserAchievement findByUserandAchievement(User user, Achievement achievement) throws ResourceNotFoundException {
        if (user == null || achievement == null) {
            throw new ResourceNotFoundException("Neither user nor achievement can be null.");
        }
        return this.userAchievementRepository.findByUserIdAndAchievementId(user.getId(), achievement.getId());
    }

    @Transactional
    public void checkAndUnlockAchievements(Integer userId) {
        User user = this.userService.findById(userId);
        List<Achievement> achievements = this.achievementService.findAll();
        List<Integer> currentAchievements = this.findByUserId(userId).stream().map(ua -> ua.getAchievement().getId()).toList();
        List<UserAchievement> toSave = new ArrayList<>();
        
        for(Achievement a:achievements) {
            boolean alreadyAchieved = currentAchievements.stream().anyMatch(ua -> ua.equals(a.getId()));
            
            if(!alreadyAchieved && this.achievementUnlocker.checkUnlock(user, a)) {
                UserAchievement ua = new UserAchievement(user, a);
                toSave.add(ua);
            }
        }
        this.saveAll(toSave);
    }

    @Transactional
    public UserAchievement unlockRules(String username) {
        User u = this.userService.findUserByName(username);
        Achievement a = this.achievementService.findById(4);
        UserAchievement ua = new UserAchievement(u, a);
        UserAchievement repeated = this.findByUserandAchievement(u, a);
        if(repeated != null) throw new IllegalArgumentException("User already has that achievement");
        return this.save(ua);
    }

    @Transactional
	public List<Achievement> findUserAchievements(Integer userId) {
		this.checkAndUnlockAchievements(userId);
		return this.findByUserId(userId).stream().map(ua -> ua.getAchievement()).toList();
	}
}