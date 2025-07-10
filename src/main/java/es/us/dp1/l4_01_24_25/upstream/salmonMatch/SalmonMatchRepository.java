package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalmonMatchRepository extends JpaRepository<SalmonMatch, Integer>{

    @Query("SELECT sm from SalmonMatch sm WHERE sm.match.id = :matchId")
    List<SalmonMatch> findAllFromMatch(@Param("matchId") Integer matchId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.player.id = :playerId")
    List<SalmonMatch> findAllFromPlayer(@Param("playerId") Integer playerId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.match.id = :matchId AND sm.coordinate.y > 20")
    List<SalmonMatch> findFromGameInSpawn(@Param("matchId") Integer matchId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchDTO(sm.id, sm.player.id, sm.salmonsNumber, sm.spawningNumber, sm.coordinate, sm.salmon, sm.match.id) FROM SalmonMatch sm WHERE sm.match.id = :matchId")
    List<SalmonMatchDTO> findAllFromMatchAsDTO(@Param("matchId") Integer matchId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchDTO(sm.id, sm.player.id, sm.salmonsNumber, sm.spawningNumber, sm.coordinate, sm.salmon, sm.match.id) FROM SalmonMatch sm WHERE sm.player.id = :playerId")
    List<SalmonMatchDTO> findAllFromPlayerAsDTO(@Param("playerId") Integer playerId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchDTO(sm.id, sm.player.id, sm.salmonsNumber, sm.spawningNumber, sm.coordinate, sm.salmon, sm.match.id) FROM SalmonMatch sm WHERE sm.match.id = :matchId AND sm.coordinate.y > 20")
    List<SalmonMatchDTO> findFromGameInSpawnAsDTO(@Param("matchId") Integer matchId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.match.id = :matchId AND sm.coordinate IS NOT NULL AND sm.coordinate.y < 20")
    List<SalmonMatch> findAllFromMatchInRiver(@Param("matchId") Integer matchId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.player.id = :playerId AND sm.coordinate IS NULL")
    List<SalmonMatch> findAllFromPlayerInSea(@Param("playerId") Integer playerId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.player.id = :playerId AND sm.coordinate IS NOT NULL AND sm.coordinate.y < 20")
    List<SalmonMatch> findAllFromPlayerInRiver(@Param("playerId") Integer playerId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.match.id = :matchId AND sm.coordinate IS NULL")
    List<SalmonMatch> findByMatchIdNoCoord(Integer matchId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.player.id = :playerId AND sm.coordinate.y > 20")
    List<SalmonMatch> findAllFromPlayerInSpawn(@Param("playerId") Integer playerId);

}
