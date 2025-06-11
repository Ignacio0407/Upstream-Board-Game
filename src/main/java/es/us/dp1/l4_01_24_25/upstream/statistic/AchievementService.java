package es.us.dp1.l4_01_24_25.upstream.statistic;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.us.dp1.l4_01_24_25.upstream.model.BaseService;

@Service
public class AchievementService extends BaseService<Achievement,Integer>{
        
    AchievementRepository repo;

    @Autowired
    public AchievementService(AchievementRepository repo){
        super(repo);
    }

    public Achievement modifyAchievement(Achievement newAchievement, int id) {
		Achievement achievementToUpdate = this.findById(id);
		
		BeanUtils.copyProperties(newAchievement, achievementToUpdate, "id");
		return this.save(achievementToUpdate);
	}
}