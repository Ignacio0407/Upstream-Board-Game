package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

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

    @GetMapping
    public ResponseEntity<List<UserAchievement>> findAll(@RequestParam(required = false) String auth) {
        List<UserAchievement> res;
        res = (List<UserAchievement>) userAchievementService.findAll();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public UserAchievement findById(@PathVariable("id") Integer id) throws ResourceNotFoundException{
    return userAchievementService.findById(id).orElseThrow(() -> new ResourceNotFoundException("UserAchievement", "id", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserAchievement> create(@RequestBody @Valid UserAchievement userAchievement) {
        UserAchievement savedUA = userAchievementService.saveUA(userAchievement);
        return new ResponseEntity<>(savedUA, HttpStatus.CREATED);
    }

    @PostMapping("/unlockrules/{username}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserAchievement> unlockRules(@PathVariable("username") String username) throws Exception {
        User u = userService.findUser(username);
        Achievement a = achievementService.findById(4);
        UserAchievement ua = new UserAchievement(u, a);
        UserAchievement repeated = userAchievementService.findByUandA(u, a);
        if(repeated != null) throw new Exception(); 
        userAchievementService.saveUA(ua);
        return new ResponseEntity<>(ua, HttpStatus.CREATED);
    }

}