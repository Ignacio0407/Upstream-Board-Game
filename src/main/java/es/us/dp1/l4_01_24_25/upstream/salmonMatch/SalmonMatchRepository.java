package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;

public interface SalmonMatchRepository extends CrudRepository<SalmonMatch, Integer>{

    @SuppressWarnings("null")
    @Override
    Optional<SalmonMatch> findById(Integer id);

    List<SalmonMatch> findAll();

    @Query("SELECT sm from SalmonMatch sm WHERE sm.match.id = :matchId")
    List<SalmonMatch> findAllFromMatch(@Param("matchId") Integer matchId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.player.id = :playerId")
    List<SalmonMatch> findAllFromPlayer(@Param("playerId") Integer playerId);
    @Query("SELECT sm from SalmonMatch sm WHERE sm.match.id = :matchId AND sm.coordinate.y > 20")
    List<SalmonMatch> findFromGameInSpawn(@Param("matchId") Integer matchId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.match.id = :matchId AND sm.coordinate IS NULL")
    List<SalmonMatch> findWithNoCoord(Integer matchId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.player.id = :playerId AND sm.coordinate.y > 20")
    List<SalmonMatch> findAllFromPlayerInSpawn(@Param("playerId") Integer playerId);

}