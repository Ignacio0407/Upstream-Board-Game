package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.general.BaseService;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileRepository;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerRepository;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchRepository;

@Service
public class MatchService extends BaseService<Match, Integer>{
        
    MatchRepository matchRepository;
    MatchTileRepository matchTileRepository;
    SalmonMatchRepository salmonMatchRepository;
    PlayerRepository playerRepository;
    PlayerService playerService;

    @Autowired
    public MatchService(MatchRepository matchRepository, MatchTileRepository matchTileRepository, SalmonMatchRepository salmonMatchRepository, PlayerRepository playerRepository, PlayerService playerService) {
        super(matchRepository);
        this.matchTileRepository = matchTileRepository;
        this.salmonMatchRepository = salmonMatchRepository;
        this.playerRepository = playerRepository;
        this.playerService = playerService;
    }

    @Transactional(readOnly = true)
    public List<Player> findPlayersFromGame(Integer id) {
        List<Player> p = matchRepository.findPlayersFromGame(id);
        if(!p.isEmpty()) return p;
        else return List.of();
    }

    @Transactional(readOnly = true)
    public Integer findPlayersNumber(Integer id) throws ResourceNotFoundException{
        List<Player> players = findPlayersFromGame(id);
        return players.size();
    }

    @Transactional
    private Match update(Match partidaNueva, Match partidaToUpdate) {
        BeanUtils.copyProperties(partidaNueva, partidaToUpdate, "id");
        return matchRepository.save(partidaToUpdate);
    }

    @Transactional
    public Match updateById(Match partidaNueva, Integer idtoUpdate) {
        Match partidaToUpdate = findById(idtoUpdate);
        if (partidaToUpdate == null){
            return null;
        }
       if (partidaToUpdate.getPlayersNumber() != null && partidaToUpdate.getPlayersNumber().equals(0)){ 
            partidaToUpdate.setState(State.FINALIZADA);
        }
        return update(partidaNueva, partidaToUpdate);
    }

    @Transactional(readOnly = true)
    public List<MatchTile> findHeronWithCoordsFromGame(Integer gameId){
        return matchRepository.findHeronWithCoordFromGame(gameId);
    }

    @Transactional
    public void checkGameHasFinished(Integer matchId) {
        Match match = matchRepository.findById(matchId).get();
        List<MatchTile> tiles = matchTileRepository.findByMatchId(matchId);
        List<SalmonMatch> salmons = salmonMatchRepository.findAllFromMatch(matchId);
        List<SalmonMatch> noCoord = salmonMatchRepository.findWithNoCoord(matchId);
        List<Player> players = playerRepository.findPlayersByMatch(matchId);
        if(tiles.isEmpty() || salmons.isEmpty() ||  (salmons.stream().filter(s -> s.getCoordinate() != null).allMatch(s -> s.getCoordinate().y() > 20) && noCoord.isEmpty()) ||  players.stream().allMatch(p -> p.getAlive() == false)
         || players.stream().allMatch(p -> playerService.checkPlayerFinished(p.getId()))) {
            match.setState(State.FINALIZADA);
            save(match);
        }
    }

    @Transactional
    public void changeInitialPlayer(Integer matchId) {
        Match match = matchRepository.findById(matchId).get();
        List<Player> players = playerRepository.findAlivePlayersByMatch(matchId).stream()
            .sorted(Comparator.comparing(Player::getPlayerOrder)).toList();
        
        Integer initialOrder = match.getInitialPlayer().getPlayerOrder();
        int currentIndex = players.indexOf(players.stream()
            .filter(player -> player.getPlayerOrder().equals(initialOrder)).findFirst().get());
    
        int nextIndex = (currentIndex + 1) % players.size();
    
        Player nextInitialPlayer = players.get(nextIndex);

        match.setInitialPlayer(nextInitialPlayer);
        matchRepository.save(match); 
    }
    

    @Transactional
    public void changePlayerTurn(Integer playerId) {
        Player player = playerRepository.findById(playerId).get();
        Match match = player.getMatch();
        List<Player> players = playerRepository.findAlivePlayersByMatch(match.getId()).stream().sorted(Comparator.comparing(Player::getPlayerOrder)).toList();
        Integer myOrder = player.getPlayerOrder();
        Integer nPlayers = players.size();
        List<MatchTile> tilesNoCoord = matchTileRepository.findWithNoCoord(match.getId());
        if(match.getRound() > 6){
            List<Player> playersFinished = playerRepository.findAlivePlayersByMatch(match.getId()).stream()
                .filter(p -> salmonMatchRepository.findAllFromPlayer(p.getId()).stream().allMatch(s -> s.getCoordinate().y()>20)).toList();
            Integer currentIndex = players.indexOf(players.stream().filter(p -> p.getPlayerOrder().equals(myOrder)).findFirst().get());
            Integer nextIndex = (currentIndex + 1) % nPlayers;
            Player nextPlayer = players.get(nextIndex);
            while (playersFinished.contains(nextPlayer)) {
                nextIndex = (nextIndex + 1) % nPlayers;
                nextPlayer = players.get(nextIndex);
                if (nextPlayer.equals(player)) {
                    // Validate aditional conditions before finalizing game
                    if (salmonMatchRepository.findAllFromPlayer(playerId).stream().allMatch(s -> s.getCoordinate().y() > 20)) {
                        match.setState(State.FINALIZADA);
                        break;
                    }
                }
            }
            match.setActualPlayer(nextPlayer);
        }

        else {
        if(players.stream().allMatch(p -> p.getEnergy() <= 0)) {
            if(!tilesNoCoord.isEmpty()) match.setPhase(Phase.CASILLAS); match.setRound(match.getRound()+1);
            changeInitialPlayer(match.getId());
            match.setActualPlayer(match.getInitialPlayer());
        }else{
            int currentIndex = players.indexOf(players.stream().filter(p -> p.getPlayerOrder().equals(myOrder)).findFirst().get());
    
        int nextIndex = (currentIndex + 1) % nPlayers;
        Player nextPlayer = players.get(nextIndex);
        match.setActualPlayer(nextPlayer);
        }
    }
        this.save(match);
    }


    
}