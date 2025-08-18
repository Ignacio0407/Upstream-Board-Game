package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MTCoordinateDTO;
import es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MatchTileDTO;
import es.us.dp1.l4_01_24_25.upstream.model.BaseRepository;

@Repository
public interface MatchTileRepository extends BaseRepository<MatchTile, MatchTileDTO, Integer>{

    @Override
    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MatchTileDTO(mt.id, mt.capacity, mt.orientation, mt.salmonsNumber, mt.coordinate, mt.tile, mt.match.id) FROM MatchTile mt WHERE mt.id = :id")
    Optional<MatchTileDTO> findByIdAsDTO(@Param("id") Integer id);
    
    @Override
    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MatchTileDTO(mt.id, mt.capacity, mt.orientation, mt.salmonsNumber, mt.coordinate, mt.tile, mt.match.id) FROM MatchTile mt")
    List<MatchTileDTO> findAllAsDTO();
    
    List<MatchTile> findByMatchId(Integer matchId);

    List<MatchTile> findByMatchIdAndCoordinateIsNull(Integer matchId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MatchTileDTO(mt.id, mt.capacity, mt.orientation, mt.salmonsNumber, mt.coordinate, mt.tile, mt.match.id) FROM MatchTile mt WHERE mt.match.id = :matchId")
    List<MatchTileDTO> findByMatchIdAsDTO(@Param("matchId") Integer matchId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MatchTileDTO(mt.id, mt.capacity, mt.orientation, mt.salmonsNumber, mt.coordinate, mt.tile, mt.match.id) FROM MatchTile mt WHERE mt.match.id = :matchId AND mt.coordinate IS NULL")
    List<MatchTileDTO> findByMatchIdAndCoordinateIsNullAsDTO(@Param("matchId") Integer matchId);

    @Query("SELECT mt FROM MatchTile mt WHERE mt.tile.type = 'HERON' AND mt.coordinate IS NOT NULL AND mt.match.id = :id")
    public List<MatchTile> findHeronWithCoordFromGame(@Param("id") Integer id);

    @Query("SELECT mt FROM MatchTile mt WHERE mt.coordinate.x = :x AND mt.coordinate.y = :y")
    Optional<MatchTile> findByCoordinate(Integer x, Integer y);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MTInternalDTO(mt.coordinate) FROM MatchTile mt where mt.match.id = :matchId AND mt.cooordinate IS NULL")
    List<MTCoordinateDTO> findByMatchIdNoCoordAsMTCoordinateDTO (Integer matchId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MTInternalDTO(mt.id, mt.coordinate) FROM MatchTile mt where mt.match.id = :matchId")
    List<MTCoordinateDTO> findByMatchIdAsMTCoordinateDTO (Integer matchId);

}