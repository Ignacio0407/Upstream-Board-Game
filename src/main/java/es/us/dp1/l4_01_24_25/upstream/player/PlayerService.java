package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.us.dp1.l4_01_24_25.upstream.exceptions.BadRequestException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.InsufficientEnergyException;
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
    SimpMessagingTemplate messagingTemplate;

    public PlayerService(PlayerRepository playerRepository, PlayerMapper playerMapper, @Lazy SalmonMatchService salmonMatchService , @Lazy UserService userService, @Lazy MatchService matchService, SimpMessagingTemplate messagingTemplate) {
        super(playerRepository, playerMapper);
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.salmonMatchService = salmonMatchService;
        this.userService = userService;
        this.matchService = matchService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Player> findPlayersByMatch(Integer id) {
        return this.playerRepository.findByMatchId(id);
    }

    public List<PlayerDTO> findPlayersByMatchAsDTO(Integer id) {
        return this.playerRepository.findPlayersByMatchAsDTO(id);
    }

    public List<PlayerDTO> findPlayersByMatchSortedPlayerOrderAsDTO(Integer id) {
        return this.playerRepository.findByMatchIdOrderByPlayerOrderAscAsDTO(id);
    }

    public List<Player> findAlivePlayersByMatch(Integer id) {
        return this.playerRepository.findByMatchIdAndAliveTrue(id);
    }

    public List<Player> findAlivePlayersByMatchSortedPlayerOrder(Integer id) {
        return this.playerRepository.findByMatchIdAndAliveTrueOrderByPlayerOrderAsc(id);
    }

    @Override
    @Transactional
    protected Player updateEntityFields (Player playerToUpdate, Player newPlayer) {
        playerToUpdate.setName(newPlayer.getName());
        playerToUpdate.setColor(newPlayer.getColor());
        playerToUpdate.setAlive(newPlayer.getAlive());
        playerToUpdate.setEnergy(newPlayer.getEnergy());
        playerToUpdate.setPoints(newPlayer.getPoints());
        playerToUpdate.setPlayerOrder(newPlayer.getPlayerOrder());
        return playerToUpdate;
    }

    @Transactional(readOnly = true)
    public Boolean isUserInMatch(String username, Integer matchId) {
        return this.playerRepository.existsByUsernameAndMatchId(username, matchId);
    }

    public void notifyNewPlayer (LobbyPlayerDTO lobbyPlayer) {
        this.messagingTemplate.convertAndSend("/topic/match/newPlayer/" + lobbyPlayer.getMatchId(), lobbyPlayer);
    }

    public void notifyPlayersUpdate(Integer matchId) { 
        List<PlayerDTO> playersDTO = this.findPlayersByMatchSortedPlayerOrderAsDTO(matchId); 
        this.messagingTemplate.convertAndSend("/topic/players/" + matchId, playersDTO); 
    }

    public void throwExceptionNotEnoughEnergy(Player player, Integer energyUsed) {
        if (player.getEnergy() - energyUsed < 0) {
            throw new InsufficientEnergyException(String.format("Insufficient energy for that move. Actual: %d, Tried using: %d", 
                player.getEnergy(), energyUsed));
        }
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
        this.matchService.notifyMatchUpdate(match);
        this.matchService.notifyMatchUpdateDashboard(match);
        this.save(p);
        LobbyPlayerDTO result = this.playerMapper.toLobby(p);
        this.notifyNewPlayer(result);
        return result;
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        Player player = this.findById(id);
        Match match = player.getMatch();
        match.setPlayersNumber(match.getPlayersNumber() - 1);
        this.matchService.save(match);
        this.matchService.notifyMatchUpdate(match);
        this.repository.deleteById(id);
    }

    public PlayerDTO updateEnergy(Integer id, Integer energyUsed) {
        Player player = this.findById(id);
        if (player.getEnergy() - energyUsed < 0) {
            throw new BadRequestException(String.format("Insufficient energy for that move. Actual: %d, Tried using: %d", 
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
    public void checkPlayersDeadOrFinished (List<Player> players) {
        for(Player p : players) {
            Boolean dead = this.checkPlayerIsDead(p.getId());
            Boolean finished = this.checkPlayerFinished(p.getId());
            if (dead) this.setPlayerDead(p);
            if (finished) this.setPlayerNoEnergy(p);
        }
    }

    @Transactional
    public Boolean checkPlayerNoEnergy(Player player) {
        return player.getEnergy() == 0;
    }

    public record NextPlayerResult(Player player, int nextIndex) {}

    public NextPlayerResult findNextPlayer (Player player, List<Player> players) {
        Integer myOrder = player.getPlayerOrder();
        Integer nPlayers = players.size();
        Integer currentIndex = players.indexOf(players.stream().filter(p -> p.getPlayerOrder().equals(myOrder)).findFirst().orElse(null));
        Integer nextIndex = (currentIndex + 1) % nPlayers;
        Player nextPlayer = players.get(nextIndex);
        return new NextPlayerResult(nextPlayer, nextIndex);
    }

    @Transactional(readOnly = true)
    public List<Player> playersForChangePhase (Integer matchId) {
        return this.findAlivePlayersByMatch(matchId).stream().filter(
            p -> !this.salmonMatchService.findByPlayerIdInRiverAsCoordinateDTO(p.getId()).isEmpty() 
            || !this.salmonMatchService.findByPlayerIdInSeaAsCoordinateDTO(p.getId()).isEmpty()).toList();
    }

}