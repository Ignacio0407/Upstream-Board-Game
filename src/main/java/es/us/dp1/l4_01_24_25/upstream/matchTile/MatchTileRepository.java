package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchTileRepository extends CrudRepository<MatchTile, Integer>{

    List<MatchTile> findAll();

    Optional<MatchTile> findById(Integer id);

    @Query("SELECT mt FROM MatchTile mt WHERE mt.match.id = :matchId"  )
    List<MatchTile> findByMatchId(Integer matchId);

}
