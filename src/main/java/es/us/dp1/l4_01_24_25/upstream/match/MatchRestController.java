package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.general.BaseRestController;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchRestController extends BaseRestController<Match, Integer>{
    
    MatchService matchService;
    PlayerService playerService;
    UserService userService;
    MatchTileService matchTileService;
    SalmonMatchService salmonMatchService;
    UserAchievementService userAchievementService;

    @Autowired
    public MatchRestController(MatchService matchService, PlayerService playerService, UserService userService, MatchTileService matchTileService, SalmonMatchService sms, UserAchievementService userAchievementService) {
        super(matchService);
        this.playerService = playerService;
        this.userService = userService;
        this.matchTileService = matchTileService;
        this.salmonMatchService = sms;
        this.userAchievementService = userAchievementService;
    }

    // TODO
    @GetMapping("/user/{id}")
    public ResponseEntity<List<Match>> findMatchesFromUser() {
        return null;
    }

    @GetMapping("/{id}/players")
    public ResponseEntity<List<Player>> findPlayersFromGame(@PathVariable("id") Integer id) throws ResourceNotFoundException {
        List<Player> l = matchService.findPlayersFromGame(id);
        if(l == null) {
            return new ResponseEntity<>(List.of(), HttpStatus.NOT_FOUND);
        } else return new ResponseEntity<>(l, HttpStatus.OK);
    }   


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ResponseEntity<Match> save(@RequestBody @Valid Match partida) {
        try {
            if (partida == null || partida.getName() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (partida.getName().length() < 3 || partida.getName().length() > 50) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(matchService.save(partida), HttpStatus.CREATED);
        } catch (DataAccessException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{matchId}/ronda")
    public ResponseEntity<Match> updateRound(@PathVariable("matchId") Integer matchId) throws ResourceNotFoundException {
        Match match = matchService.findById(matchId);
        if (match == null) {
            throw new ResourceNotFoundException("Partida no encontrada", "id", matchId.toString());
        }
        match.setRound(match.getRound()+1);
        return new ResponseEntity<>(match, HttpStatus.OK);
    }

    @PostMapping("/matchCreator/{userId}")
    public ResponseEntity<Match> createMatchWMatchCreator(@PathVariable("userId") Integer userId, @RequestBody Map<String, String> requestBody) { 
    User u = userService.findById(userId);
    if (u == null) return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    String name = requestBody.getOrDefault("name", "");
    String password = requestBody.getOrDefault("password", "");
    Match m = new Match();
    m.setName(name);
    m.setPassword(password);
    m.setMatchCreator(u);
    m.setState(State.ESPERANDO);
    m.setPlayersNum(0);
    m.setRound(0);
    m.setPhase(Phase.CASILLAS);
    m.setInitialPlayer(null);
    m.setActualPlayer(null);
    m.setFinalScoreCalculated(false);
    matchService.save(m);
    return new ResponseEntity<>(m, HttpStatus.CREATED);
}


@PatchMapping("/{matchId}/changephase/{playerId}")
public ResponseEntity<Match> changePhase(@PathVariable("matchId") Integer matchId, @PathVariable("playerId") Integer playerId) {
    Match match = matchService.findById(matchId);
    if (match == null) throw new ResourceNotFoundException("No se ha podido encontrar la partida");
    Player player = playerService.findById(playerId);
    Phase phase = match.getPhase();
    List<MatchTile> mtNoC = matchTileService.findByMatchIdNoCoord(matchId);
    List<Player> players = playerService.findAlivePlayersByMatch(matchId).stream()
    .filter(p -> !salmonMatchService.getAllFromPlayerInRiver(p.getId()).isEmpty() || !salmonMatchService.getAllFromPlayerInSea(p.getId()).isEmpty()).toList();
    Integer round = match.getRound();

    if(mtNoC.size() == 0 && phase == Phase.CASILLAS){
        players.stream().forEach(p -> p.setEnergy(5));
        for(Player p : players) playerService.save(p);
        match.setPhase(Phase.MOVIENDO);
        match.setActualPlayer(match.getInitialPlayer());
    }
    else if(mtNoC.size() == 0) {
        match.setPhase(Phase.MOVIENDO);
        if(playerService.checkPlayerNoEnergy(playerId)){
            matchService.changePlayerTurn(playerId);
        }
        if(players.stream().allMatch(p -> p.getEnergy() <= 0)) {
            List<SalmonMatch> salmonMatchesInSpawn = salmonMatchService.getSalmonsInSpawnFromGame(matchId);
            if(!salmonMatchesInSpawn.isEmpty() && salmonMatchesInSpawn != null) {
                List<SalmonMatch> salmonMatchesInSpawnExceptLast = salmonMatchesInSpawn.stream().filter(s -> s.getCoordinate().y() < 25).toList();
                salmonMatchesInSpawnExceptLast.stream().forEach(s -> {
                s.setCoordinate(new Coordinate(s.getCoordinate().x(), s.getCoordinate().y() + 1));
                salmonMatchService.save(s);});
            }
            players.stream().forEach(p -> p.setEnergy(5));
            for(Player p : players) playerService.save(p);
            match.setRound(round+1);
        }
    }
    else if(phase.equals(Phase.CASILLAS)) {
        List<Integer> rds = List.of(17, 14, 11, 8, 5, 2, 0);
        if (rds.contains(mtNoC.size())) {
            players.stream().forEach(p -> p.setEnergy(5));
            for(Player p : players) playerService.save(p);
            match.setPhase(Phase.MOVIENDO);
            match.setActualPlayer(match.getInitialPlayer());
        }
        if (match.getRound() == 0 && mtNoC.size() != 17) matchService.changePlayerTurn(playerId);
    }
    else {
     if (playerService.checkPlayerNoEnergy(playerId)) {
        matchService.changePlayerTurn(playerId); 
    }


    }

    if (playerService.checkPlayerIsDead(playerId)) { 
        matchService.changePlayerTurn(playerId);
        playerService.setPlayerDead(playerId);
        
    } else if (playerService.checkPlayerFinished(playerId)) {
        playerService.setPlayerNoEnergy(playerId); 
        matchService.changePlayerTurn(playerId);
    }
    /**
     * Muere (sigue teniendo energia)
     * Muere (se queda justo sin energia)
     * Termina (sigue teniendo energia)
     * Termina (no tiene mas energia)
     */

    endRound(matchId);
    matchService.save(match);
    matchService.checkGameHasFinished(matchId);
    return new ResponseEntity<>(match, HttpStatus.OK);
    }

    @PatchMapping("/{matchId}/startGame")
    public ResponseEntity<Match> startGame(@PathVariable("matchId") Integer matchId) throws ResourceNotFoundException {
        Match match = matchService.findById(matchId);
        if (match == null) throw new ResourceNotFoundException("Ha habido un problema encontrando la partida");
        List<Player> player = playerService.findPlayersByMatch(matchId);

        match.setState(State.EN_CURSO);
        match.setActualPlayer(player.get(0));
        match.setInitialPlayer(player.get(0));
        match.setPlayersNumber(player.size());
        matchService.save(match);
        return new ResponseEntity<>(match, HttpStatus.OK);
    }

    private ResponseEntity<Match> endRound(@PathVariable("matchId") Integer matchId) throws ResourceNotFoundException {
        Match partida = matchService.findById(matchId);
        List<MatchTile> mt = matchTileService.findByMatchId(matchId);
        List<Integer> tilesPerRound = List.of(17, 14, 11, 8, 5, 2);
        List<MatchTile> mtNoc = matchTileService.findByMatchIdNoCoord(matchId);
        List<SalmonMatch> salmonMatches = salmonMatchService.getAllFromMatch(matchId);
        Integer round = partida.getRound();
        Phase phase = partida.getPhase();
        List<Player> players = playerService.findPlayersByMatch(matchId);
        if(round == 2){ 
            List<SalmonMatch> mtInOcean = salmonMatches.stream().filter(m -> m.getCoordinate()==null).toList();
            for (SalmonMatch sm: mtInOcean) { 
                deleteSalmon(sm.getId());
            }
            for(Player p : players) {
                Boolean dead = playerService.checkPlayerIsDead(p.getId());
                Boolean finished = playerService.checkPlayerFinished(p.getId());
                if (dead) playerService.setPlayerDead(p.getId());
                if (finished) playerService.setPlayerNoEnergy(p.getId());
            }
        }

        else if (round > 7 && mtNoc.size() == 0){
            List<SalmonMatch> mtOutOfPosition = salmonMatches.stream().filter(m -> m.getCoordinate().y() == (round - 8)).toList();
            for(SalmonMatch sm:mtOutOfPosition) {
                deleteSalmon(sm.getId());
                playerService.checkPlayerIsDead(sm.getPlayer().getId());
            } 
            for(MatchTile m:mt) {
                if(m.getCoordinate().y() == (round - 8)){
                    matchTileService.delete(m.getId());
                }
            }
            for(Player p : players) {
                Boolean dead = playerService.checkPlayerIsDead(p.getId());
                Boolean finished = playerService.checkPlayerFinished(p.getId());
                if (dead) playerService.setPlayerDead(p.getId());
                if (finished) playerService.setPlayerNoEnergy(p.getId());
            }
        }
        
        else if(round>2 && tilesPerRound.contains(mtNoc.size()) && phase == Phase.CASILLAS){

            for(SalmonMatch sm:salmonMatches) {
                if(sm.getCoordinate().y()==0){
                    salmonMatchService.delete(sm.getId());
                    playerService.checkPlayerIsDead(sm.getPlayer().getId());
                }else{
                    //m.setCoordinate(new Coordinate(m.getCoordinate().x(), m.getCoordinate().y()-1));
                    Coordinate oldCoord = sm.getCoordinate();
                    Coordinate newCoord = new Coordinate(oldCoord.x(), oldCoord.y() - 1);
                    sm.setCoordinate(newCoord);
                    salmonMatchService.save(sm);

                }
            }

            for(MatchTile m:mt) {
                if(m.getCoordinate().y()==0){
                    matchTileService.delete(m.getId());
                    //m.setCoordinate(new Coordinate(m.getCoordinate().x(), 99));
                }
                else{
                    //m.setCoordinate(new Coordinate(m.getCoordinate().x(), m.getCoordinate().y()-1));
                    Coordinate oldCoord = m.getCoordinate();
                    Coordinate newCoord = new Coordinate(oldCoord.x(), oldCoord.y() - 1);
                    m.setCoordinate(newCoord);
                    matchTileService.save(m);
                }
            }
            for(Player p : players) {
                Boolean dead = playerService.checkPlayerIsDead(p.getId());
                Boolean finished = playerService.checkPlayerFinished(p.getId());
                if (dead) playerService.setPlayerDead(p.getId());
                if (finished) playerService.setPlayerNoEnergy(p.getId());
            }
        }
        return new ResponseEntity<>(partida, HttpStatus.OK);
    }
    
    private void deleteSalmon(Integer id){
        salmonMatchService.delete(id);
    }

    @PatchMapping("/finalscore/{id}")
    public ResponseEntity<Match> finalScore(@PathVariable Integer id) {
        List<Player> players = playerService.findPlayersByMatch(id);
        Match match = matchService.findById(id);
        
        if (match.getFinalScoreCalculated() == true) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(match);
        }

        for(Player p : players){
            User u = p.getUserPlayer();
            List<SalmonMatch> files = salmonMatchService.getSalmonsFromPlayerInSpawn(p.getId());
            u.setPlayedgames(u.getPlayedgames() + 1);
            if(!files.isEmpty()){
            Integer totalPoints = 0;
            Integer salmonPoints = files.stream().mapToInt(s -> salmonMatchService.getPointsFromASalmonInSpawn(s.getId())).sum();
            Integer filePoints = files.stream().mapToInt(sM -> sM.getSalmonsNumber()).sum();
            totalPoints = salmonPoints + filePoints;
            p.setPoints(totalPoints);
            u.setTotalpoints(u.getTotalpoints() + totalPoints);
            playerService.save(p);
            userService.save(u);
            }
            userAchievementService.checkAndUnlockAchievements(u.getId());
        }
        Optional<Player> winner = players.stream().max(Comparator.comparing(Player::getPoints));
        if(winner.isPresent()) {
            User winnerUser = winner.get().getUserPlayer();
            winnerUser.setVictories(winnerUser.getVictories() + 1);
            userService.save(winnerUser);
        }
        match.setFinalScoreCalculated(true);
        matchService.save(match);
        return new ResponseEntity<>(match, HttpStatus.OK);
    }


}