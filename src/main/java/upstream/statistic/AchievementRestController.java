package upstream.statistic;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import upstream.model.BaseRestController;
import upstream.userachievement.UserAchievementService;

@RestController
@RequestMapping("/api/v1/achievements")
public class AchievementRestController extends BaseRestController<Achievement,Integer>{
    
    AchievementService achievementService;
	UserAchievementService userAchievementService;

    @Autowired
	public AchievementRestController(AchievementService achievementService, UserAchievementService userAchievementService) {
		super(achievementService);
		this.userAchievementService = userAchievementService;
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Achievement>> findUserAchievements(@PathVariable("userId") int userId) {
		return new ResponseEntity<>(this.userAchievementService.findUserAchievements(userId), HttpStatus.OK);
	}
}