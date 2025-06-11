package es.us.dp1.l4_01_24_25.upstream.statistic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.model.BaseRestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/achievements")
public class AchievementRestController extends BaseRestController<Achievement,Integer>{
    
    AchievementService achievementService;

    @Autowired
	public AchievementRestController(AchievementService achievementService) {
		super(achievementService);
	}

	@PutMapping("update/{id}")
	public ResponseEntity<Achievement> modifyAchievement(@RequestBody @Valid Achievement newAchievement, @PathVariable("id") int id) {
		return new ResponseEntity<>(achievementService.modifyAchievement(newAchievement,id), HttpStatus.OK);
	}
}