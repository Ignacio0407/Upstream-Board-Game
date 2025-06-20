package es.us.dp1.l4_01_24_25.upstream.statistic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.model.BaseRestController;

@RestController
@RequestMapping("/api/v1/achievements")
public class AchievementRestController extends BaseRestController<Achievement,Integer>{
    
    AchievementService achievementService;

    @Autowired
	public AchievementRestController(AchievementService achievementService) {
		super(achievementService);
	}
}