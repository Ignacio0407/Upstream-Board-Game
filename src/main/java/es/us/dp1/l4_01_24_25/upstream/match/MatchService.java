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
    public List<Match> getAll() {
        return matchRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Match> getSomeByName(List<String> names) {
        List<Match> partidas = new LinkedList<>();
        names.stream().forEach(name -> partidas.add(geByName(name)));
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
    public Match getById(Integer id) {
        Optional <Match> op = matchRepository.findById(id);
        return optionalToValueOrNull(op);
    }

    @Transactional(readOnly = true)
    public Match geByName(String name) {
        Optional <Match> op = Optional.ofNullable(matchRepository.findByName(name));
        return optionalToValueOrNull(op);
    }

    @Transactional
    public void deleteAll() {
        matchRepository.deleteAll();
    }

    @Transactional
    public void deleteSomeById(List<Integer> idsToDelete) {
        idsToDelete.stream().forEach( id -> deletePartidaById(id));
    }

    @Transactional
    public void deletePartidaById(Integer id) {
        getById(id);
        matchRepository.deleteById(id);

    }


    @Transactional
    private Match update(Match partidaNueva, Match partidaToUpdate) {
        BeanUtils.copyProperties(partidaNueva, partidaToUpdate, "id");
        return matchRepository.save(partidaToUpdate);
    }

    @Transactional
    public Match updateById(Match partidaNueva, Integer idtoUpdate) {
        Match partidaToUpdate = getById(idtoUpdate);
        if (partidaToUpdate == null){
            return null;
        }
       if (partidaToUpdate.getPlayersNum() != null && partidaToUpdate.getPlayersNum().equals(0)){ 
            partidaToUpdate.setState(State.FINALIZADA);
        }
        return update(partidaNueva, partidaToUpdate);
    }


    @Transactional
	public Match save(Match partida) {
		matchRepository.save(partida);
		return partida;
	}
    
}
