package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("override")
public interface  PlayerRepository extends CrudRepository<Player, Integer>{
    
    List<Player> findAll();

    public Optional<Player> findById(Integer id);
    
    public Player findByName(String name);

    @Query("SELECT p FROM Player p WHERE p.match.id = :id")
    public List<Player> findPlayersByMatch(Integer id);

    @Query("SELECT p FROM Player p WHERE p.match.id = :matchId AND p.alive = TRUE")
    List<Player> findAlivePlayersByMatch(@Param("matchId") Integer matchId);
}
