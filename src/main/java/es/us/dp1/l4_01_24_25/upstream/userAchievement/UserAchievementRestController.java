package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/usersachievements")
@SecurityRequirement(name="bearerAuth")
public class UserAchievementRestController {
    
    private final UserAchievementService userAchievementService;
    private final UserService userService;
    private final AchievementService achievementService;
    @Autowired
    public UserAchievementRestController(UserAchievementService userAchievementService, UserService userService, AchievementService achievementService, AchievementChecker achievementChecker) {
        this.userAchievementService = userAchievementService;
        this.userService = userService;
        this.achievementService = achievementService;
    }

    @PostMapping("/unlockrules/{username}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserAchievement> unlockRules(@PathVariable("username") String username) throws Exception {
        User u = userService.findUser(username);
        Achievement a = achievementService.findById(4);
        UserAchievement ua = new UserAchievement(u, a);
        UserAchievement repeated = userAchievementService.findByUandA(u, a);
        if(repeated != null) throw new Exception(); 
        userAchievementService.save(ua);
        return new ResponseEntity<>(ua, HttpStatus.CREATED);
    }

}