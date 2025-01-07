package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface salmonMatchRepository extends CrudRepository<salmonMatch, Integer>{
    
    @SuppressWarnings("null")
    @Override
    Optional<salmonMatch> findById(Integer id);

    List<salmonMatch> findAll();
    
    @Query("SELECT sm from salmonMatch sm WHERE sm.match.id = :matchId")
    List<salmonMatch> findAllFromMatch(@Param("matchId") Integer matchId);

    @Query("SELECT sm from salmonMatch sm WHERE sm.player.id = :playerId")
    List<salmonMatch> findAllFromPlayer(@Param("playerId") Integer playerId);
}
