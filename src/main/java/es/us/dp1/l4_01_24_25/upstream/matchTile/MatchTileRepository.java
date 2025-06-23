package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchTileRepository extends JpaRepository<MatchTile, Integer>{

    @Query("SELECT mt FROM MatchTile mt WHERE mt.match.id = :matchId")
    List<MatchTile> findByMatchId(Integer matchId);

    @Query("SELECT mt FROM MatchTile mt WHERE mt.match.id = :matchId AND mt.coordinate IS NULL")
    List<MatchTile> findByMatchIdNoCoord(Integer matchId);

    @Query("SELECT mt FROM MatchTile mt WHERE mt.coordinate.x = :x AND mt.coordinate.y = :y")
    Optional<MatchTile> findByCoordinate(Integer x, Integer y);

}
