package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("override")
public interface MatchTileRepository extends CrudRepository<MatchTile, Integer>{

    List<MatchTile> findAll();

    Optional<MatchTile> findById(Integer id);

    @Query("SELECT mt FROM MatchTile mt WHERE mt.match.id = :matchId")
    List<MatchTile> findByMatchId(Integer matchId);

    @Query("SELECT mt FROM MatchTile mt WHERE mt.match.id = :matchId AND mt.coordinate IS NULL")
    List<MatchTile> findWithNoCoord(Integer matchId);

    @Query("SELECT mt FROM MatchTile mt WHERE mt.coordinate.x = :x AND mt.coordinate.y = :y")
    MatchTile findByCoordinate(Integer x, Integer y);

}
