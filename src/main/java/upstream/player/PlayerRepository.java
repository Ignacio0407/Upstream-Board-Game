package upstream.player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import upstream.model.BaseRepository;
import upstream.player.playerdto.PlayerDTO;

@Repository
public interface PlayerRepository extends BaseRepository<Player, PlayerDTO, Integer>{

    @Override
    @Query("SELECT new upstream.player.playerdto.PlayerDTO(p.id, p.name, p.color, p.playerOrder, p.alive, p.points, p.energy, p.userPlayer.id, p.match.id) FROM Player p where p.id = :id")
    Optional<PlayerDTO> findByIdAsDTO(Integer id);

    @Override
    @Query("SELECT new upstream.player.playerdto.PlayerDTO(p.id, p.name, p.color, p.playerOrder, p.alive, p.points, p.energy, p.userPlayer.id, p.match.id) FROM Player p")
    List<PlayerDTO> findAllAsDTO();

    public Optional<Player> findByName(String name);

    public List<Player> findByMatchId(Integer id);

    Player findByUserPlayer_IdAndMatch_Id(Integer userId, Integer matchId);

    public List<Player> findByMatchIdOrderByPlayerOrderAsc(Integer id);

    @Query("SELECT new upstream.player.playerdto.PlayerDTO(p.id, p.name, p.color, p.playerOrder, p.alive, p.points, p.energy, p.userPlayer.id, p.match.id) FROM Player p WHERE p.match.id = :id")
    List<PlayerDTO> findPlayersByMatchAsDTO(@Param("id") Integer id);

    @Query("SELECT new upstream.player.playerdto.PlayerDTO(p.id, p.name, p.color, p.playerOrder, p.alive, p.points, p.energy, p.userPlayer.id, p.match.id) FROM Player p WHERE p.match.id = :id ORDER BY p.playerOrder ASC")
    List<PlayerDTO> findByMatchIdOrderByPlayerOrderAscAsDTO(@Param("id") Integer id);

    List<Player> findByMatchIdAndAliveTrue(@Param("matchId") Integer matchId);

    List<Player> findByMatchIdAndAliveTrueOrderByPlayerOrderAsc(@Param("matchId") Integer matchId);

    @Query("SELECT COUNT(p) > 0 FROM Player p WHERE p.userPlayer.name = :username AND p.match.id = :matchId")
    Boolean existsByUsernameAndMatchId(@Param("username") String username, @Param("matchId") Integer matchId);

}