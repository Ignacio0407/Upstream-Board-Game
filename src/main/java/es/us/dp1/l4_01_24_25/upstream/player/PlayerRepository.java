package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO;

@Repository
public interface  PlayerRepository extends JpaRepository<Player, Integer>{
    
    public Optional<Player> findByName(String name);

    @Query("SELECT p FROM Player p WHERE p.match.id = :id")
    public List<Player> findPlayersByMatch(Integer id);

    @Query("SELECT p FROM Player p WHERE p.match.id = :id ORDER BY p.playerOrder ASC")
    public List<Player> findPlayersByMatchSortedPlayerOrder(Integer id);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO(p.id, p.name, p.color, p.playerOrder, p.alive, p.points, p.energy, p.userPlayer.id, p.match.id) FROM Player p WHERE p.match.id = :id")
    List<PlayerDTO> findPlayersByMatchAsDTO(@Param("id") Integer id);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO(p.id, p.name, p.color, p.playerOrder, p.alive, p.points, p.energy, p.userPlayer.id, p.match.id) FROM Player p WHERE p.match.id = :id ORDER BY p.playerOrder ASC")
    List<PlayerDTO> findPlayersByMatchSortedPlayerOrderAsDTO(@Param("id") Integer id);

    @Query("SELECT p FROM Player p WHERE p.match.id = :matchId AND p.alive = TRUE")
    List<Player> findAlivePlayersByMatch(@Param("matchId") Integer matchId);

    @Query("SELECT p FROM Player p WHERE p.match.id = :matchId AND p.alive = TRUE ORDER BY p.playerOrder ASC")
    List<Player> findAlivePlayersByMatchSortedPlayerOrder(@Param("matchId") Integer matchId);

}