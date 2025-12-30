package es.us.dp1.l4_01_24_25.upstream.statistic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.model.BaseService;

@Service
public class AchievementService extends BaseService<Achievement,Integer>{
        
    AchievementRepository repo;

    @Autowired
    public AchievementService(AchievementRepository repo){
        super(repo);
    }

    @Override
    @Transactional
    protected void updateEntityFields(Achievement existingEntity, Achievement updatedEntity) {
        existingEntity.setName(updatedEntity.getName());
        existingEntity.setDescription(updatedEntity.getDescription());
        existingEntity.setBadgeImage(updatedEntity.getBadgeImage());
        existingEntity.setThreshold(updatedEntity.getThreshold());
        existingEntity.setMetric(updatedEntity.getMetric());
    }
}