package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.player.Player;

@Repository
public interface MatchRepository extends CrudRepository<Match, Integer>{
    
    @SuppressWarnings("null")
    @Override
    List<Match> findAll();
    
    public Match findByName(String name);

    @Query("SELECT p FROM Player p WHERE p.match.id = :id")
    public List<Player> findPlayersFromGame(@Param("id") Integer id);

    @Query("SELECT mt FROM MatchTile mt WHERE mt.tile.type.type = 'GARZA' AND mt.coordinate IS NOT NULL AND mt.match.id = :id")
    public List<MatchTile> findHeronWithCoordFromGame(@Param("id") Integer id);

}