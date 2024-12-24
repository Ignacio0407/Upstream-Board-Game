package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;

@Service
public class PlayerService {
        
    PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Player> getJugadores() {
        return playerRepository.findAll();
    }


    // COMPLETAR MANEJO ERRORES
    @Transactional(readOnly = true)
    public List<Player> getSomeById(List<Integer> ids) {
        List<Player> Jugadores = new LinkedList<>();
        ids.stream().forEach(id -> Jugadores.add(getJugadorById(id)));
        //throw new ResourceNotFoundException("Jugadores no encontradas");
        return new ArrayList<>(Jugadores);
    }

    @Transactional(readOnly = true)
    public List<Player> getSomeJugadoresByName(List<String> names) {
        List<Player> Jugadores = new LinkedList<>();
        names.stream().forEach(name -> Jugadores.add(getJugadorByName(name)));
        return new ArrayList<>(Jugadores);
    }

    @Transactional(readOnly = true)
    public List<Player> getPlayersByMatch(Integer id) {
        List<Player> jugadores = playerRepository.findPlayersByMatch(id);

        return jugadores.isEmpty()? new ArrayList<>() : jugadores;

    }
    private Player optionalToValueWithNotFoundException(Optional<Player> op) {
        if (!op.isPresent()) {
            throw new ResourceNotFoundException("No existe la Jugador indicada");
        }
        return op.get();
    }

    @Transactional(readOnly = true)
    public Player getJugadorById(Integer id) {
        Optional <Player> op = playerRepository.findById(id);
        return optionalToValueWithNotFoundException(op);
    }

    @Transactional(readOnly = true)
    public Player getJugadorByName(String name) {
        Optional <Player> op = Optional.ofNullable(playerRepository.findByName(name));
        return optionalToValueWithNotFoundException(op);
    }

    @Transactional
    public void deleteAllJugadores() {
        playerRepository.deleteAll();
    }

    @Transactional
    public void deleteSomeById(List<Integer> idsToDelete) {
        idsToDelete.stream().forEach( id -> deleteJugadorById(id));
    }

    @Transactional
    public void deleteSomeJugadoresByName(List<String> namesToDelete) {
        namesToDelete.stream().forEach( name -> deleteJugadorByName(name));
    }

    @Transactional
    public void deleteJugadorById(Integer id) {
        getJugadorById(id); // Si no existe p, ya lanza la excepcion.
        playerRepository.deleteById(id);

    }

    @Transactional
    public void deleteJugadorByName(String name) {
        Player p = getJugadorByName(name); // Si no existe p, ya lanza la excepcion.
        playerRepository.delete(p);
    }


    @Transactional
    private Player updateJugador(Player JugadorNueva, Player JugadorToUpdate) {
        BeanUtils.copyProperties(JugadorNueva, JugadorToUpdate, "id");
        return playerRepository.save(JugadorToUpdate); // Guarda y retorna la versión actualizada
    }

    @Transactional
    public Player updateJugadorById(Player JugadorNueva, Integer idtoUpdate) {
        Player JugadorToUpdate = getJugadorById(idtoUpdate); // Si no existe p, ya lanza la excepcion.
        return updateJugador(JugadorNueva, JugadorToUpdate);
    }

    @Transactional  
    public Player updateJugadorByName(Player JugadorNueva, String nameToUpdate) {
        Player JugadorToUpdate = getJugadorByName(nameToUpdate); // Si no existe p, ya lanza la excepcion.
        return updateJugador(JugadorNueva, JugadorToUpdate);
    }


    @Transactional
	public Player saveJugador(Player jugador) throws DataAccessException {
		playerRepository.save(jugador);
		return jugador;
	}


    @Transactional
	public List<Player> saveJugadores(List<Player> Jugadores) throws DataAccessException {
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

    public List<SalmonMatch> getSalmonsByPlayerId(Integer playerId) { 
        return playerRepository.findSalmonMatchesByPlayer(playerId);
    }

}
