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
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileRepository;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerRepository;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchRepository;

@Service
public class MatchService {
        
    MatchRepository matchRepository;
    MatchTileRepository matchTileRepository;
    SalmonMatchRepository salmonMatchRepository;
    PlayerRepository playerRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository, MatchTileRepository matchTileRepository, SalmonMatchRepository salmonMatchRepository, PlayerRepository playerRepository) {
        this.matchRepository = matchRepository;
        this.matchTileRepository = matchTileRepository;
        this.salmonMatchRepository = salmonMatchRepository;
        this.playerRepository = playerRepository;
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

    @Transactional(readOnly = true)
    public List<MatchTile> getHeronWithCoordsFromGame(Integer gameId){

        return matchRepository.findHeronWithCoordFromGame(gameId);

    }

    @Transactional
	public Match save(Match partida) {
		matchRepository.save(partida);
		return partida;
	}

    @Transactional
    public void checkGameHasFinished(Integer matchId) {
        Match match = matchRepository.findById(matchId).get();
        List<MatchTile> tiles = matchTileRepository.findByMatchId(matchId);
        List<SalmonMatch> salmons = salmonMatchRepository.findAllFromMatch(matchId);
        List<SalmonMatch> noCoord = salmonMatchRepository.findWithNoCoord(matchId);
        List<Player> players = playerRepository.findPlayersByMatch(matchId);
        if(tiles.isEmpty() || salmons.isEmpty() || (salmons.stream().filter(s -> s.getCoordinate() != null).allMatch(s -> s.getCoordinate().y() > 20) && noCoord.isEmpty()) || players.stream().allMatch(p -> p.getAlive() == false)) {
            match.setState(State.FINALIZADA);
            save(match);
        }
    }

    @Transactional
    public void changePlayerOrder(Integer matchId) {
        Match match = matchRepository.findById(matchId).get();
        List<Player> players = playerRepository.findAlivePlayersByMatch(matchId);
        Integer playersN = players.size();
        players.stream().forEach(p -> {p.setPlayerOrder((p.getPlayerOrder() - 1 + playersN)%playersN); playerRepository.save(p);});
        Player ini = players.stream().filter(p -> p.getPlayerOrder() == 0).findFirst().get();
        match.setActualPlayer(ini);
        save(match);
    }

    @Transactional
    public void changePlayerTurn(Integer playerId) {
        Player player = playerRepository.findById(playerId).get();
        Match match = player.getMatch();
        List<Player> players = playerRepository.findAlivePlayersByMatch(match.getId());
        Integer myOrder = player.getPlayerOrder();
        Integer nPlayers = players.size();
        Player nextPlayer = players.stream().filter(p -> p.getPlayerOrder().equals((myOrder+1)%nPlayers)).findFirst().get();
        if(match.getPhase().equals(Phase.MOVIENDO) && player.getEnergy().equals(0)) match.setActualPlayer(nextPlayer);
        if(match.getPhase().equals(Phase.CASILLAS)) match.setActualPlayer(nextPlayer);
        save(match);
    }
    
}