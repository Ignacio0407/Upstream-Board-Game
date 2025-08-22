package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.us.dp1.l4_01_24_25.upstream.model.BaseRepository;
import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO;

@Repository
public interface PlayerRepository extends BaseRepository<Player, PlayerDTO, Integer>{

    @Override
    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO(p.id, p.name, p.color, p.playerOrder, p.alive, p.points, p.energy, p.userPlayer.id, p.match.id) FROM Player p where p.id = :id")
    Optional<PlayerDTO> findByIdAsDTO(Integer id);

    @Override
    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO(p.id, p.name, p.color, p.playerOrder, p.alive, p.points, p.energy, p.userPlayer.id, p.match.id) FROM Player p")
    List<PlayerDTO> findAllAsDTO();

    public Optional<Player> findByName(String name);

    public List<Player> findByMatchId(Integer id);

    public List<Player> findByMatchIdOrderByPlayerOrderAsc(Integer id);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO(p.id, p.name, p.color, p.playerOrder, p.alive, p.points, p.energy, p.userPlayer.id, p.match.id) FROM Player p WHERE p.match.id = :id")
    List<PlayerDTO> findPlayersByMatchAsDTO(@Param("id") Integer id);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO(p.id, p.name, p.color, p.playerOrder, p.alive, p.points, p.energy, p.userPlayer.id, p.match.id) FROM Player p WHERE p.match.id = :id ORDER BY p.playerOrder ASC")
    List<PlayerDTO> findByMatchIdOrderByPlayerOrderAscAsDTO(@Param("id") Integer id);

    List<Player> findByMatchIdAndAliveTrue(@Param("matchId") Integer matchId);

    List<Player> findByMatchIdAndAliveTrueOrderByPlayerOrderAsc(@Param("matchId") Integer matchId);

}