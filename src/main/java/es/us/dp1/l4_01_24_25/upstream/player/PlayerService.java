package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.us.dp1.l4_01_24_25.upstream.exceptions.BadRequestException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.model.BaseService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@Service
public class PlayerService extends BaseService<Player,Integer>{
        
    PlayerRepository playerRepository;
    SalmonMatchService salmonMatchService ;
    UserService userService;
    MatchService matchService;

    public PlayerService(PlayerRepository playerRepository, SalmonMatchService  salmonMatchService , UserService userService, MatchService matchService) {
        super(playerRepository);
        this.salmonMatchService = salmonMatchService ;
        this.userService = userService;
        this.matchService = matchService;
    }

    @Transactional(readOnly = true)
    public List<Player> findPlayersByMatch(Integer id) {
        return this.findList(playerRepository.findPlayersByMatch(id));
    }

    @Transactional(readOnly = true)
    public List<Player> findAlivePlayersByMatch(Integer id) {
        return this.findList(playerRepository.findAlivePlayersByMatch(id));
    }

    @Transactional
    private Player update (Player JugadorNueva, Player JugadorToUpdate) {
        BeanUtils.copyProperties(JugadorNueva, JugadorToUpdate, "id");
        return playerRepository.save(JugadorToUpdate);
    }

    @Transactional
    public Player updateById (Player JugadorNueva, Integer idtoUpdate) {
        Player JugadorToUpdate = this.findById(idtoUpdate);
        return update(JugadorNueva, JugadorToUpdate);
    }

    public Player createPlayerInMatch(Integer matchId, Map<String,String> requestBody) {
        String idUser = requestBody.getOrDefault("user", "");
        User user = userService.findById(Integer.valueOf(idUser));
        String color = requestBody.getOrDefault("color", "");
        Match match = matchService.findById(matchId);
        Player p = new Player();
        p.setName(user.getName());
        p.setColor(Color.valueOf(color));
        p.setAlive(true);
        p.setEnergy(5);
        p.setUserPlayer(user);
        p.setMatch(match);
        p.setPoints(0);
        p.setPlayerOrder(match.getPlayersNumber());
        match.setPlayersNumber(match.getPlayersNumber() + 1);
        matchService.save(match);

        return this.save(p);
    }

    public Player updateEnergy(Integer id, Integer energyUsed) throws ResourceNotFoundException, Exception {
        Player player = this.findById(id);
        if (player.getEnergy() - energyUsed < 0) {
            throw new BadRequestException(String.format("Insuficient energy for that move. Actual: %d, Tried using: %d", 
                player.getEnergy(), energyUsed));
        }
        player.setEnergy(player.getEnergy() - energyUsed);
        return this.save(player);
    }

    public Player regenerateEnergy(Integer id) {
        Player player = this.findById(id);
        player.setEnergy(5);
        return this.save(player);
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