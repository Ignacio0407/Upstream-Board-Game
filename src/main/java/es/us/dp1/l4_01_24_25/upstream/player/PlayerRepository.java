package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import es.us.dp1.l4_01_24_25.upstream.salmonMatch.salmonMatch;

@Repository
public interface  PlayerRepository extends CrudRepository<Player, Integer>{
    
    List<Player> findAll();

    public Optional<Player> findById(Integer id);
    
    public Player findByName(String name);

    @Query("SELECT p FROM Player p WHERE p.match.id = :id")
    public List<Player> findPlayersByMatch(Integer id);

    @Query("SELECT sm FROM salmonMatch sm WHERE sm.player.id = :id")
    public List<salmonMatch> findSalmonMatchesByPlayer(Integer id);
}
