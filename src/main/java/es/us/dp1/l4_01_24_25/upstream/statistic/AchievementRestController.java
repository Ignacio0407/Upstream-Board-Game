package es.us.dp1.l4_01_24_25.upstream.statistic;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.general.BaseRestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/achievements")
public class AchievementRestController extends BaseRestController<Achievement,Integer>{
    
    AchievementService achievementService;

    @Autowired
	public AchievementRestController(AchievementService achievementService) {
		super(achievementService);
	}

	@SuppressWarnings("null")
	@PutMapping("update/{id}")
	public ResponseEntity<Void> modifyAchievement(@RequestBody @Valid Achievement newAchievement, @PathVariable("id") int id) {
		Achievement achievementToUpdate = this.findById(id).getBody();

		if (newAchievement.getId() == null || !newAchievement.getId().equals(id)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		BeanUtils.copyProperties(newAchievement, achievementToUpdate, "id");
		achievementService.save(achievementToUpdate);
		
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}