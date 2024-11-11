package es.us.dp1.l4_01_24_25.upstream.statistic;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/achievements")
@Tag(name = "Achievements", description = "The Achievements management API")
@SecurityRequirement(name = "bearerAuth")
public class AchievementRestController {
    
    private final AchievementService achievementService;

    @Autowired
	public AchievementRestController(AchievementService achievementService) {
		this.achievementService = achievementService;
	}

    @GetMapping
	public ResponseEntity<List<Achievement>> findAll() {
		return new ResponseEntity<>((List<Achievement>) achievementService.getAchievements(), HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Achievement> findAchievement(@PathVariable("id") int id){
		Achievement achievementToGet=achievementService.getById(id);
		if(achievementToGet==null)
			throw new ResourceNotFoundException("Achievement with id "+id+" not found!");
		return new ResponseEntity<>(achievementToGet, HttpStatus.OK);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<Achievement> createAchievement(@RequestBody @Valid Achievement newAchievement) { 
		Achievement result = achievementService.saveAchievement(newAchievement);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@SuppressWarnings("null")
	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<Void> modifyAchievement(@RequestBody @Valid Achievement newAchievement, @PathVariable("id") int id) {
		Achievement achievementToUpdate = this.findAchievement(id).getBody();
		
		if (newAchievement.getId() == null || !newAchievement.getId().equals(id)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		BeanUtils.copyProperties(newAchievement, achievementToUpdate, "id");
		achievementService.saveAchievement(achievementToUpdate);
		
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}


	@DeleteMapping("/{id}")
	@PreAuthorize("ADMIN")
	public ResponseEntity<Void> deleteAchievement(@PathVariable("id") int id){
		findAchievement(id);
		achievementService.deleteAchievementById(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
