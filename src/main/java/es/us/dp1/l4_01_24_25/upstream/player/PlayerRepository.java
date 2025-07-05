package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface  PlayerRepository extends JpaRepository<Player, Integer>{

    public Optional<Player> findByName(@Param("name") String name);
    
    @Query("SELECT p FROM Player p WHERE p.match.id = :id")
    public List<Player> findPlayersByMatch(Integer id);

    @Query("SELECT p FROM Player p WHERE p.match.id = :id ORDER BY p.playerOrder ASC")
    public List<Player> findPlayersByMatchSortedPlayerOrder(Integer id);

    @Query("SELECT p FROM Player p WHERE p.match.id = :matchId AND p.alive = TRUE")
    List<Player> findAlivePlayersByMatch(@Param("matchId") Integer matchId);
}
