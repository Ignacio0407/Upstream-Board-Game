package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;

@Repository
public interface MatchRepository extends JpaRepository<Match, Integer>{

    public Optional<Match> findByName(@Param("name") String name);

    @Query("SELECT mt FROM MatchTile mt WHERE mt.tile.type.type = 'GARZA' AND mt.coordinate IS NOT NULL AND mt.match.id = :id")
    public List<MatchTile> findHeronWithCoordFromGame(@Param("id") Integer id);

}