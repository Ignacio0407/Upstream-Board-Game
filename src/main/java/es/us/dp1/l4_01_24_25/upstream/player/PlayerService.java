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
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SMCoordinateDTO;
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
        return this.playerRepository.findByMatchId(id);
    }

    @Transactional(readOnly = true)
    public List<PlayerDTO> findPlayersByMatchAsDTO(Integer id) {
        return this.playerRepository.findPlayersByMatchAsDTO(id);
    }

    @Transactional(readOnly = true)
    public List<PlayerDTO> findPlayersByMatchSortedPlayerOrderAsDTO(Integer id) {
        return this.playerRepository.findByMatchIdOrderByPlayerOrderAscAsDTO(id);
    }

    @Transactional(readOnly = true)
    public List<Player> findAlivePlayersByMatch(Integer id) {
        return this.playerRepository.findByMatchIdAndAliveTrue(id);
    }

    @Transactional(readOnly = true)
    public List<Player> findAlivePlayersByMatchSortedPlayerOrder(Integer id) {
        return this.playerRepository.findByMatchIdAndAliveTrueOrderByPlayerOrderAsc(id);
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
        User user = this.userService.findById(Integer.valueOf(playerDTO.get("user")));
        Match match = this.matchService.findById(matchId);
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
        this.matchService.save(match);
        this.save(p);
        return this.playerMapper.toLobby(p);
    }

    public PlayerDTO updateEnergy(Integer id, Integer energyUsed) {
        Player player = this.findById(id);
        if (player.getEnergy() - energyUsed < 0) {
            throw new BadRequestException(String.format("Insuficient energy for that move. Actual: %d, Tried using: %d", 
                player.getEnergy(), energyUsed));
        }
        player.setEnergy(player.getEnergy() - energyUsed);
        this.save(player);
        return this.playerMapper.toDTO(player);
    }

    public PlayerDTO regenerateEnergy(Integer id) {
        Player player = this.findById(id);
        player.setEnergy(5);
        this.save(player);
        return this.playerMapper.toDTO(player);
    }

    @Transactional
    public void setPlayerDead(Player player) {
        player.setAlive(false);
        player.setEnergy(0); 
        player.setPlayerOrder(10); 
        this.save(player);
    }

    @Transactional
    public void setPlayerNoEnergy(Player player) {
        player.setEnergy(0);
        this.save(player);
    }

    @Transactional
    public Boolean checkPlayerFinished(Integer playerId) {
        List<SMCoordinateDTO> salmons = this.salmonMatchService.findByPlayerIdAsCoordinateDTO(playerId);
        return !salmons.isEmpty() && salmons.stream().allMatch(s -> s.getCoordinate() != null && s.getCoordinate().y() > 20);
    }

    @Transactional
    public Boolean checkPlayerIsDead(Integer playerId) {
        return this.salmonMatchService.findByPlayerIdAsCoordinateDTO(playerId).isEmpty();
    }

    @Transactional
    public Boolean checkPlayerNoEnergy(Player player) {
        return player.getEnergy() == 0;
    }

    public record NextPlayerResult(Player player, int nextIndex) {}

    public NextPlayerResult findNextPlayer (Player player, List<Player> players) {
        Integer myOrder = player.getPlayerOrder();
        Integer nPlayers = players.size();
        Integer currentIndex = players.indexOf(players.stream().filter(p -> p.getPlayerOrder().equals(myOrder)).findFirst().get());
        Integer nextIndex = (currentIndex + 1) % nPlayers;
        Player nextPlayer = players.get(nextIndex);
        return new NextPlayerResult(nextPlayer, nextIndex);
    }

    public List<Player> playersForChangePhase (Integer matchId) {
        return this.findAlivePlayersByMatch(matchId).stream().filter(
            p -> !this.salmonMatchService.findByPlayerIdInRiverAsCoordinateDTO(p.getId()).isEmpty() 
            || !this.salmonMatchService.findByPlayerIdInSeaAsCoordinateDTO(p.getId()).isEmpty()).toList();
    }

}