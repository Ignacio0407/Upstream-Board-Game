package es.us.dp1.l4_01_24_25.upstream.statistic;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Integer>{
    
    public Optional<Achievement> findByName(@Param("name") String name);
}