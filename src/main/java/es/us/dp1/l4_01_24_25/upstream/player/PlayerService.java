package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchRepository;

@Service
public class PlayerService {
        
    PlayerRepository playerRepository;
    SalmonMatchRepository salmonMatchRepository;

    public PlayerService(PlayerRepository playerRepository, SalmonMatchRepository salmonMatchRepository) {
        this.playerRepository = playerRepository;
        this.salmonMatchRepository = salmonMatchRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Player> getJugadores() {
        return playerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Player getById(Integer id) {
        Optional <Player> op = playerRepository.findById(id);
        return optionalToValueOrNull(op);
    }

    @Transactional(readOnly = true)
    public Player getByName(String name) {
        Optional <Player> op = Optional.ofNullable(playerRepository.findByName(name));
        return optionalToValueOrNull(op);
    }

    @Transactional(readOnly = true)
    public List<Player> getSomeByName (List<String> names) {
        List<Player> Jugadores = new LinkedList<>();
        names.stream().forEach(name -> Jugadores.add(getByName(name)));
        return new ArrayList<>(Jugadores);
    }

    @Transactional(readOnly = true)
    public List<Player> getPlayersByMatch(Integer id) {
        List<Player> jugadores = playerRepository.findPlayersByMatch(id);

        return jugadores.isEmpty()? new ArrayList<>() : jugadores;

    }

    @Transactional(readOnly = true)
    public List<Player> getAlivePlayersByMatch(Integer id) {
        List<Player> jugadores = playerRepository.findAlivePlayersByMatch(id);
        if(jugadores == null) return List.of();
        return jugadores;
    }

    private Player optionalToValueOrNull(Optional<Player> op) {
        if (!op.isPresent()) {
            return null;
        }
        return op.get();
    }


    @Transactional
    public void deleteById (Integer id) {
        getById(id);
        playerRepository.deleteById(id);

    }

    @Transactional
    private Player update (Player JugadorNueva, Player JugadorToUpdate) {
        BeanUtils.copyProperties(JugadorNueva, JugadorToUpdate, "id");
        return playerRepository.save(JugadorToUpdate);
    }

    @Transactional
    public Player updateById (Player JugadorNueva, Integer idtoUpdate) {
        Player JugadorToUpdate = getById(idtoUpdate);
        return update(JugadorNueva, JugadorToUpdate);
    }


    @Transactional
	public Player savePlayer(Player jugador) throws DataAccessException {
		playerRepository.save(jugador);
		return jugador;
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
        Player player = playerRepository.findById(playerId).get();
        player.setAlive(false);
        player.setEnergy(0); 
        player.setPlayerOrder(10); 
        playerRepository.save(player);
    }

    @Transactional
    public void setPlayerNoEnergy(Integer playerId) {
        Player player = playerRepository.findById(playerId).get();
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
        Player player = playerRepository.findById(playerId).get();
        Boolean res = false;
        if(player.getEnergy() == 0) res = true;
        return res;
    }

}