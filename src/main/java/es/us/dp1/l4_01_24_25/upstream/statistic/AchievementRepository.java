package es.us.dp1.l4_01_24_25.upstream.statistic;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository extends CrudRepository<Achievement, Integer>{
    
    @SuppressWarnings("override")
    List<Achievement> findAll();
    
    public Achievement findByName(String name);
}

