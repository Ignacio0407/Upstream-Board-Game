package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SalmonMatchRepository extends CrudRepository<SalmonMatch, Integer>{
    
    @SuppressWarnings("null")
    @Override
    Optional<SalmonMatch> findById(Integer id);

    List<SalmonMatch> findAll();
    
    @Query("SELECT sm from SalmonMatch sm WHERE sm.match.id = :matchId")
    List<SalmonMatch> findAllFromMatch(@Param("matchId") Integer matchId);

    @Query("SELECT sm from SalmonMatch sm WHERE sm.player.id = :playerId")
    List<SalmonMatch> findAllFromPlayer(@Param("playerId") Integer playerId);
}
