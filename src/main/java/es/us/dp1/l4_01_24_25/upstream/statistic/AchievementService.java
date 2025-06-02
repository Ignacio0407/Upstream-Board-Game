package es.us.dp1.l4_01_24_25.upstream.statistic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.us.dp1.l4_01_24_25.upstream.general.BaseService;

@Service
public class AchievementService extends BaseService<Achievement,Integer>{
        
    AchievementRepository repo;

    @Autowired
    public AchievementService(AchievementRepository repo){
        super(repo);
    }
}