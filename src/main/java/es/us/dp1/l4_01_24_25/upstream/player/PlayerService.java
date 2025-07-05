package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.us.dp1.l4_01_24_25.upstream.exceptions.BadRequestException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.model.BaseServiceWithDTO;
import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO;
import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.LobbyPlayerDTO;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@Service
public class PlayerService extends BaseServiceWithDTO<Player, PlayerDTO, Integer>{
        
    PlayerRepository playerRepository;
    PlayerMapper playerMapper;
    SalmonMatchService salmonMatchService;
    UserService userService;
    MatchService matchService;

    public PlayerService(PlayerRepository playerRepository, PlayerMapper playerMapper, @Lazy SalmonMatchService salmonMatchService , @Lazy UserService userService, @Lazy MatchService matchService) {
        super(playerRepository, playerMapper);
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.salmonMatchService = salmonMatchService;
        this.userService = userService;
        this.matchService = matchService;
    }

    @Transactional(readOnly = true)
    public List<Player> findPlayersByMatch(Integer id) {
        return this.findList(playerRepository.findPlayersByMatch(id));
    }

    @Transactional(readOnly = true)
    public List<PlayerDTO> findPlayersByMatchAsDTO(Integer id) {
        return this.findListDTO(this.findPlayersByMatch(id), playerMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<PlayerDTO> findPlayersByMatchSortedPlayerOrderAsDTO(Integer id) {
        return this.findListDTO(this.findList(this.playerRepository.findPlayersByMatchSortedPlayerOrder(id)), playerMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<Player> findAlivePlayersByMatch(Integer id) {
        return this.findList(playerRepository.findAlivePlayersByMatch(id));
    }

    @Transactional(readOnly = true)
    public List<PlayerDTO> findAlivePlayersByMatchAsDTO(Integer id) {
        return this.findListDTO(this.findAlivePlayersByMatch(id), playerMapper::toDTO);
    }

    @Override
    @Transactional
    protected void updateEntityFields (Player newPlayer, Player playerToUpdate) {
        playerToUpdate.setName(newPlayer.getName());
        playerToUpdate.setColor(newPlayer.getColor());
        playerToUpdate.setAlive(newPlayer.getAlive());
        playerToUpdate.setEnergy(newPlayer.getEnergy());
        playerToUpdate.setPoints(newPlayer.getPoints());
        playerToUpdate.setPlayerOrder(newPlayer.getPlayerOrder());
    }

    public LobbyPlayerDTO createPlayerInMatch(Integer matchId, Map<String,String> playerDTO) {
        User user = userService.findById(Integer.valueOf(playerDTO.get("user")));
        Match match = matchService.findById(matchId);
        Player p = new Player();
        p.setName(user.getName());
        p.setColor(Color.valueOf(playerDTO.get("color")));
        p.setAlive(true);
        p.setEnergy(5);
        p.setUserPlayer(user);
        p.setMatch(match);
        p.setPoints(0);
        p.setPlayerOrder(match.getPlayersNumber());
        match.setPlayersNumber(match.getPlayersNumber() + 1);
        matchService.save(match);
        this.save(p);
    return playerMapper.toLobby(p);
    }

    public PlayerDTO updateEnergy(Integer id, Integer energyUsed) {
        Player player = this.findById(id);
        if (player.getEnergy() - energyUsed < 0) {
            throw new BadRequestException(String.format("Insuficient energy for that move. Actual: %d, Tried using: %d", 
                player.getEnergy(), energyUsed));
        }
        player.setEnergy(player.getEnergy() - energyUsed);
        this.save(player);
        return playerMapper.toDTO(player);
    }

    public PlayerDTO regenerateEnergy(Integer id) {
        Player player = this.findById(id);
        player.setEnergy(5);
        this.save(player);
        return playerMapper.toDTO(player);
    }

    @Transactional
    public void setPlayerDead(Integer playerId) {
        Player player = this.findById(playerId);
        player.setAlive(false);
        player.setEnergy(0); 
        player.setPlayerOrder(10); 
        this.save(player);
    }

    @Transactional
    public void setPlayerNoEnergy(Integer playerId) {
        Player player = this.findById(playerId);
        player.setEnergy(0);
        this.save(player);

    }

    @Transactional
    public Boolean checkPlayerFinished(Integer playerId) {
        List<SalmonMatch> salmons = salmonMatchService.findAllFromPlayer(playerId);
        return !salmons.isEmpty() && salmons.stream().allMatch(s -> s.getCoordinate() != null && s.getCoordinate().y() > 20);
    }

    @Transactional
    public Boolean checkPlayerIsDead(Integer playerId) {
        return salmonMatchService.findAllFromPlayer(playerId).isEmpty();
    }

    @Transactional
    public Boolean checkPlayerNoEnergy(Integer playerId){
        Player player = this.findById(playerId);
        return player.getEnergy() == 0;
    }
}