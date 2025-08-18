package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.us.dp1.l4_01_24_25.upstream.model.BaseRepository;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SMCoordinateDTO;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SalmonMatchDTO;

public interface SalmonMatchRepository extends BaseRepository<SalmonMatch, SalmonMatchDTO, Integer>{

    List<SalmonMatch> findByMatchId(@Param("matchId") Integer matchId);

    List<SalmonMatch> findByPlayerId(@Param("playerId") Integer playerId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.match.id = :matchId AND sm.coordinate.y > 20")
    List<SalmonMatch> findFromGameInSpawn(@Param("matchId") Integer matchId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SalmonMatchDTO(sm.id, sm.player.id, sm.salmonsNumber, sm.spawningNumber, sm.coordinate, sm.salmon, sm.match.id) FROM SalmonMatch sm WHERE sm.match.id = :matchId")
    List<SalmonMatchDTO> findByMatchIdAsDTO(@Param("matchId") Integer matchId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SalmonMatchDTO(sm.id, sm.player.id, sm.salmonsNumber, sm.spawningNumber, sm.coordinate, sm.salmon, sm.match.id) FROM SalmonMatch sm WHERE sm.player.id = :playerId")
    List<SalmonMatchDTO> findByPlayerIdAsDTO(@Param("playerId") Integer playerId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SalmonMatchDTO(sm.id, sm.player.id, sm.salmonsNumber, sm.spawningNumber, sm.coordinate, sm.salmon, sm.match.id) FROM SalmonMatch sm WHERE sm.match.id = :matchId AND sm.coordinate.y > 20")
    List<SalmonMatchDTO> findFromGameInSpawnAsDTO(@Param("matchId") Integer matchId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.match.id = :matchId AND sm.coordinate IS NOT NULL AND sm.coordinate.y < 20")
    List<SalmonMatch> findAllFromMatchInRiver(@Param("matchId") Integer matchId);

    List<SalmonMatch> findByPlayerIdAndCoordinateIsNull(@Param("playerId") Integer playerId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.player.id = :playerId AND sm.coordinate IS NOT NULL AND sm.coordinate.y < 20")
    List<SalmonMatch> findAllFromPlayerInRiver(@Param("playerId") Integer playerId);

    List<SalmonMatch> findByMatchIdAndCoordinateIsNull(Integer matchId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.player.id = :playerId AND sm.coordinate.y > 20")
    List<SalmonMatch> findAllFromPlayerInSpawn(@Param("playerId") Integer playerId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SMCoordinateDTO(sm.coordinate) FROM SalmonMatch sm where sm.player.id = :playerId")
    List<SMCoordinateDTO> findByPlayerIdAsCoordinateDTO(@Param("playerId") Integer playerId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SMCoordinateDTO(sm.coordinate) FROM SalmonMatch sm where sm.player.id = :playerId AND sm.coordinate IS NOT NULL AND sm.coordinate.y < 20")
    List<SMCoordinateDTO> findByPlayerIdInRiverAsCoordinateDTO(@Param("playerId") Integer playerId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SMCoordinateDTO(sm.coordinate) FROM SalmonMatch sm where sm.player.id = :playerId AND sm.coordinate IS NULL")
    List<SMCoordinateDTO> findByPlayerIdInSeaAsCoordinateDTO(@Param("playerId") Integer playerId);
}
