package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.player.Player;

@Service
public class MatchService {
        
    MatchRepository matchRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Match> getPartidas() {
        return matchRepository.findAll();
    }

    // Buscar varias partidas a la vez
    @Transactional(readOnly = true)
    public List<Match> getSomePartidasByName(List<String> names) {
        List<Match> partidas = new LinkedList<>();
        names.stream().forEach(name -> partidas.add(getPartidaByName(name)));
        return new ArrayList<>(partidas);
    }

    @Transactional(readOnly = true)
    public List<Player> getPlayersFromGame(Integer id) {
        List<Player> p = matchRepository.findPlayersFromGame(id);
        if(!p.isEmpty()) return p;
        else return List.of();
    }

    @Transactional(readOnly = true)
    public Integer getNumjugadores(Integer id) throws ResourceNotFoundException{
        List<Player> players = getPlayersFromGame(id);
        return players.size();
    }

    /* Aunque el manejo de errores de operaciones CRUD se realice en el controller, pongo solamente este
       aquí porque simplifica muchísimo la gestión de errores de bastantes de los métodos implementados. */
    private Match optionalToValueOrNull(Optional<Match> op) {
        if (!op.isPresent()) {
            return null;
        }
        return op.get();
    }

    @Transactional(readOnly = true)
    public Match getPartidaById(Integer id) {
        Optional <Match> op = matchRepository.findById(id);
        return optionalToValueOrNull(op);
    }

    @Transactional(readOnly = true)
    public Match getPartidaByName(String name) {
        Optional <Match> op = Optional.ofNullable(matchRepository.findByName(name));
        return optionalToValueOrNull(op);
    }

    @Transactional
    public void deleteAllPartidas() {
        matchRepository.deleteAll();
    }

    @Transactional
    public void deleteSomePartidasById(List<Integer> idsToDelete) {
        idsToDelete.stream().forEach( id -> deletePartidaById(id));
    }

    @Transactional
    public void deletePartidaById(Integer id) {
        getPartidaById(id); // Si no existe p, ya lanza la excepcion.
        matchRepository.deleteById(id);

    }


    @Transactional
    private Match updatePartida(Match partidaNueva, Match partidaToUpdate) {
        BeanUtils.copyProperties(partidaNueva, partidaToUpdate, "id");
        return matchRepository.save(partidaToUpdate); // Guarda y retorna la versión actualizada
    }

    @Transactional
    public Match updatePartidaById(Match partidaNueva, Integer idtoUpdate) {
        Match partidaToUpdate = getPartidaById(idtoUpdate);
        if (partidaToUpdate == null){
            return null;
        }
       if (partidaToUpdate.getPlayersNum() != null && partidaToUpdate.getPlayersNum().equals(0)){ 
            partidaToUpdate.setState(State.FINALIZADA);
        }
        return updatePartida(partidaNueva, partidaToUpdate);
    }


    @Transactional
	public Match savePartida(Match partida) {
		matchRepository.save(partida);
		return partida;
	}
    
}
