package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  PlayerRepository extends CrudRepository<Player, Integer>{
    
    List<Player> findAll();

    public Player findByName(String name);

    @Query("SELECT p FROM Player p WHERE p.match.id = :id")
    public List<Player> findPlayersByMatch(Integer id);
}
