package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.model.BaseRestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/usersachievements")
@SecurityRequirement(name="bearerAuth")
public class UserAchievementRestController extends BaseRestController<UserAchievement,Integer>{
    
    UserAchievementService userAchievementService;
    
    @Autowired
    public UserAchievementRestController(UserAchievementService userAchievementService, AchievementChecker achievementChecker) {
        super(userAchievementService);
        this.userAchievementService = userAchievementService;
    }

    @PostMapping("/unlockrules/{username}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserAchievement> unlockRules(@PathVariable("username") String username) throws Exception {
        return new ResponseEntity<>(userAchievementService.unlockRules(username), HttpStatus.CREATED);
    }

}