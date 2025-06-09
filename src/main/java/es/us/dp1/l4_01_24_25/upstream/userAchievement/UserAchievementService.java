package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.general.BaseService;
import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementRepository;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementUnlocker;
import es.us.dp1.l4_01_24_25.upstream.user.UserRepository;
import jakarta.transaction.Transactional;
import es.us.dp1.l4_01_24_25.upstream.user.User;

@Service
public class UserAchievementService extends BaseService<UserAchievement,Integer>{

    UserRepository userRepository;
    AchievementRepository achievementRepository;
    UserAchievementRepository userAchievementRepository;
    AchievementUnlocker achievementUnlocker = new AchievementUnlocker();

    @Autowired
    public UserAchievementService(UserRepository userRepository, AchievementRepository achievementRepository, UserAchievementRepository userAchievementRepository) {
        super(userAchievementRepository);
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
    }

    @Transactional
    public UserAchievement findByUandA(User u, Achievement a) throws ResourceNotFoundException {
    if (u == null || a == null) {
        throw new ResourceNotFoundException("Usuario o logro no pueden ser nulos.");
    }

        User pu = userRepository.findById(u.getId()).orElseThrow(() -> 
        new ResourceNotFoundException("Usuario con id: " + u.getId() + " no encontrado"));
        Achievement pa = achievementRepository.findById(a.getId()).orElseThrow(() -> 
        new ResourceNotFoundException("Logro con id: " + a.getId() + " no encontrado"));

    return userAchievementRepository.findRepeatedUserAchievement(pu, pa);
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