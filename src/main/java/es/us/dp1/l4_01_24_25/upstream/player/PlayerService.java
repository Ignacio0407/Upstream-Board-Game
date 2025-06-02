package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.general.BaseService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchRepository;

@Service
public class PlayerService extends BaseService<Player,Integer>{
        
    PlayerRepository playerRepository;
    SalmonMatchRepository salmonMatchRepository;

    public PlayerService(PlayerRepository playerRepository, SalmonMatchRepository salmonMatchRepository) {
        super(playerRepository);
        this.salmonMatchRepository = salmonMatchRepository;
    }

    @Transactional(readOnly = true)
    public List<Player> findPlayersByMatch(Integer id) {
        List<Player> jugadores = playerRepository.findPlayersByMatch(id);
        return jugadores.isEmpty()? new ArrayList<>() : jugadores;
    }

    @Transactional(readOnly = true)
    public List<Player> findAlivePlayersByMatch(Integer id) {
        List<Player> jugadores = playerRepository.findAlivePlayersByMatch(id);
        if(jugadores == null) return List.of();
        return jugadores;
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

    @Transactional
	public List<Player> savePlayers (List<Player> Jugadores) throws DataAccessException {
        List<Player> jugadoresFallidas = new LinkedList<>();
        Jugadores.forEach(jugador -> {
            try {
                playerRepository.save(jugador);
            } catch (DataAccessException e) {
                jugadoresFallidas.add(jugador);
            }
        });
		return jugadoresFallidas;
	}

    @Transactional
    public void setPlayerDead(Integer playerId) {
        Player player = this.findById(playerId);
        player.setAlive(false);
        player.setEnergy(0); 
        player.setPlayerOrder(10); 
        playerRepository.save(player);
    }

    @Transactional
    public void setPlayerNoEnergy(Integer playerId) {
        Player player = this.findById(playerId);
        player.setEnergy(0);
        playerRepository.save(player);

    }

    @Transactional
    public Boolean checkPlayerFinished(Integer playerId) {
        List<SalmonMatch> salmons = salmonMatchRepository.findAllFromPlayer(playerId);
        Boolean res = false;
        if (!salmons.isEmpty() && 
            salmons.stream().allMatch(s -> s.getCoordinate() != null && s.getCoordinate().y() > 20)) {
            res = true;
        }
        return res;
    }

    @Transactional
    public Boolean checkPlayerIsDead(Integer playerId) {
        List<SalmonMatch> salmons = salmonMatchRepository.findAllFromPlayer(playerId);
        Boolean res = false;
        if(salmons.isEmpty()) { res = true;}
        return res;
    }

    @Transactional
    public Boolean checkPlayerNoEnergy(Integer playerId){
        Player player = this.findById(playerId);
        Boolean res = false;
        if(player.getEnergy() == 0) res = true;
        return res;
    }
}