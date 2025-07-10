package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ConflictException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.model.BaseServiceWithDTO;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementService;

@Service
public class MatchService extends BaseServiceWithDTO<Match, MatchDTO, Integer>{
        
    MatchRepository matchRepository;
    MatchMapper matchMapper;
    PlayerService playerService;
    UserService userService;
    SalmonMatchService salmonMatchService;
    UserAchievementService userAchievementService;
    MatchTileService matchTileService;

    @Autowired
    public MatchService(MatchRepository matchRepository, MatchMapper matchMapper, @Lazy PlayerService playerService, @Lazy UserService userService, @Lazy SalmonMatchService salmonMatchService, @Lazy UserAchievementService userAchievementService, @Lazy MatchTileService matchTileService) {
        super(matchRepository, matchMapper);
        this.matchRepository = matchRepository;
        this.matchMapper = matchMapper;
        this.playerService = playerService;
        this.userService = userService;
        this.salmonMatchService = salmonMatchService;
        this.userAchievementService = userAchievementService;
        this.matchTileService = matchTileService;
    }

    @Override
    @Transactional
    protected void updateEntityFields(Match newMatch, Match matchToUpdate) {
        matchToUpdate.setPassword(newMatch.getPassword());
        matchToUpdate.setState(newMatch.getState());
        matchToUpdate.setPlayersNumber(newMatch.getPlayersNumber());
        matchToUpdate.setRound(newMatch.getRound());
        matchToUpdate.setPhase(newMatch.getPhase());
        matchToUpdate.setFinalScoreCalculated(newMatch.getFinalScoreCalculated());
        matchToUpdate.setInitialPlayer(newMatch.getInitialPlayer());
        matchToUpdate.setActualPlayer(newMatch.getActualPlayer());
        matchToUpdate.setMatchCreator(newMatch.getMatchCreator());
    }

    public MatchDTO createMatchWMatchCreator(Integer userId, Map<String, String> requestBody) { 
        User u = userService.findById(userId);
        String name = requestBody.getOrDefault("name", "");
        String password = requestBody.getOrDefault("password", "");
        Match m = new Match();
        m.setName(name);
        m.setPassword(password);
        m.setMatchCreator(u);
        m.setState(State.WAITING);
        m.setPlayersNumber(0);
        m.setRound(0);
        m.setPhase(Phase.TILES);
        m.setInitialPlayer(null);
        m.setActualPlayer(null);
        m.setFinalScoreCalculated(false);
        this.save(m);
        return matchMapper.toDTO(m);
    }

    public MatchDTO startGame(@PathVariable("matchId") Integer matchId) throws ResourceNotFoundException {
        Match match = this.findById(matchId);
        List<Player> player = playerService.findPlayersByMatch(matchId);
        match.setState(State.ON_GOING);
        match.setActualPlayer(player.get(0));
        match.setInitialPlayer(player.get(0));
        match.setPlayersNumber(player.size());
        this.save(match);
        return matchMapper.toDTO(match);
    }

    public MatchDTO updateRound(Integer matchId) {
        Match match = this.findById(matchId);
        match.setRound(match.getRound()+1);
        this.save(match);
        return matchMapper.toDTO(match);
    }

    @Transactional
    public void changeInitialPlayer(Integer matchId) {
        Match match = this.findById(matchId);
        List<Player> players = playerService.findAlivePlayersByMatch(matchId).stream()
            .sorted(Comparator.comparing(Player::getPlayerOrder)).toList();
        
        Integer initialOrder = match.getInitialPlayer().getPlayerOrder();
        int currentIndex = players.indexOf(players.stream()
            .filter(player -> player.getPlayerOrder().equals(initialOrder)).findFirst().get());
    
        int nextIndex = (currentIndex + 1) % players.size();
    
        Player nextInitialPlayer = players.get(nextIndex);

        match.setInitialPlayer(nextInitialPlayer);
        this.save(match); 
    }    

    @Transactional
    public void changePlayerTurn(Player player) {
        Match match = player.getMatch();
        List<Player> players = playerService.findAlivePlayersByMatchSortedPlayerOrder(match.getId());
        Integer myOrder = player.getPlayerOrder();
        Integer nPlayers = players.size();
        List<MatchTile> tilesNoCoord = matchTileService.findByMatchIdNoCoord(match.getId());
        if(match.getRound() > 6){
            List<Player> playersFinished = playerService.findAlivePlayersByMatch(match.getId()).stream()
                .filter(p -> salmonMatchService.findAllFromPlayer(p.getId()).stream().allMatch(s -> s.getCoordinate().y()>20)).toList();
            Integer currentIndex = players.indexOf(players.stream().filter(p -> p.getPlayerOrder().equals(myOrder)).findFirst().get());
            Integer nextIndex = (currentIndex + 1) % nPlayers;
            Player nextPlayer = players.get(nextIndex);
            while (playersFinished.contains(nextPlayer)) {
                nextIndex = (nextIndex + 1) % nPlayers;
                nextPlayer = players.get(nextIndex);
                if (nextPlayer.equals(player)) {
                    // Validate aditional conditions before finalizing game
                    if (salmonMatchService.findAllFromPlayer(player.getId()).stream().allMatch(s -> s.getCoordinate().y() > 20)) {
                        match.setState(State.FINALIZED);
                        break;
                    }
                }
            }
            match.setActualPlayer(nextPlayer);
        }

        else {
        if(players.stream().allMatch(p -> p.getEnergy() <= 0)) {
            if(!tilesNoCoord.isEmpty()) {
                match.setPhase(Phase.TILES); 
                match.setRound(match.getRound()+1);
            }
            this.changeInitialPlayer(match.getId());
            match.setActualPlayer(match.getInitialPlayer());
        }
        else{
            int currentIndex = players.indexOf(players.stream().filter(p -> p.getPlayerOrder().equals(myOrder)).findFirst().get());
    
        int nextIndex = (currentIndex + 1) % nPlayers;
        Player nextPlayer = players.get(nextIndex);
        match.setActualPlayer(nextPlayer);
        }
    }
        this.save(match);
    }

    private Match endRound(Integer matchId) throws ResourceNotFoundException {
        Match match = this.findById(matchId);
        List<MatchTile> mt = matchTileService.findByMatchId(matchId);
        List<Integer> tilesPerRound = List.of(17, 14, 11, 8, 5, 2);
        List<MatchTile> mtNoc = matchTileService.findByMatchIdNoCoord(matchId);
        List<SalmonMatch> salmonMatches = salmonMatchService.findAllFromMatch(matchId);
        Integer round = match.getRound();
        Phase phase = match.getPhase();
        List<Player> players = playerService.findPlayersByMatch(matchId);
        if(round == 2){ 
            List<SalmonMatch> mtInOcean = salmonMatches.stream().filter(m -> m.getCoordinate()==null).toList();
            for (SalmonMatch sm: mtInOcean) { 
                this.delete(sm.getId());
            }
            for(Player p : players) {
                Boolean dead = playerService.checkPlayerIsDead(p.getId());
                Boolean finished = playerService.checkPlayerFinished(p.getId());
                if (dead) playerService.setPlayerDead(p);
                if (finished) playerService.setPlayerNoEnergy(p);
            }
        }

        else if (round > 7 && mtNoc.size() == 0){
            List<SalmonMatch> mtOutOfPosition = salmonMatches.stream().filter(m -> m.getCoordinate().y() == (round - 8)).toList();
            for(SalmonMatch sm:mtOutOfPosition) {
                this.delete(sm.getId());
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
                if (dead) playerService.setPlayerDead(p);
                if (finished) playerService.setPlayerNoEnergy(p);
            }
        }
        
        else if(round>2 && tilesPerRound.contains(mtNoc.size()) && phase == Phase.TILES) {

            for (SalmonMatch sm:salmonMatches) {
                if(sm.getCoordinate().y()==0){
                    salmonMatchService.delete(sm.getId());
                    playerService.checkPlayerIsDead(sm.getPlayer().getId());
                } else {
                    Coordinate oldCoord = sm.getCoordinate();
                    Coordinate newCoord = new Coordinate(oldCoord.x(), oldCoord.y() - 1);
                    sm.setCoordinate(newCoord);
                    salmonMatchService.save(sm);
                }
            }

            for(MatchTile m:mt) {
                if(m.getCoordinate().y()==0){
                    matchTileService.delete(m.getId());
                }
                else{
                    Coordinate oldCoord = m.getCoordinate();
                    Coordinate newCoord = new Coordinate(oldCoord.x(), oldCoord.y() - 1);
                    m.setCoordinate(newCoord);
                    matchTileService.save(m);
                }
            }
            for(Player p : players) {
                Boolean dead = playerService.checkPlayerIsDead(p.getId());
                Boolean finished = playerService.checkPlayerFinished(p.getId());
                if (dead) playerService.setPlayerDead(p);
                if (finished) playerService.setPlayerNoEnergy(p);
            }
        }
        return match;
    }

    @Transactional
    public MatchDTO changePhase(Integer matchId, Integer playerId) {
        Match match = this.findById(matchId);
        Player player = playerService.findById(playerId);
        Phase phase = match.getPhase();
        List<MatchTile> mtNoC = matchTileService.findByMatchIdNoCoord(matchId);
        List<Player> players = playerService.findAlivePlayersByMatch(matchId).stream()
        .filter(p -> !salmonMatchService.findAllFromPlayerInRiver(p.getId()).isEmpty() || !salmonMatchService.findAllFromPlayerInSea(p.getId()).isEmpty()).toList();
        Integer round = match.getRound();

        if(mtNoC.size() == 0 && phase == Phase.TILES) {
            players.stream().forEach(p -> p.setEnergy(5));
            playerService.saveAll(players);
            match.setPhase(Phase.MOVING);
            match.setActualPlayer(match.getInitialPlayer());
        }

        else if(mtNoC.size() == 0) {
            match.setPhase(Phase.MOVING);
            if(playerService.checkPlayerNoEnergy(player)){
                this.changePlayerTurn(player);
            }
            if(players.stream().allMatch(p -> p.getEnergy() <= 0)) {
                List<SalmonMatch> salmonMatchesInSpawn = salmonMatchService.findFromGameInSpawn(matchId);
                if(salmonMatchesInSpawn != null && !salmonMatchesInSpawn.isEmpty()) {
                    List<SalmonMatch> salmonMatchesInSpawnExceptLast = salmonMatchesInSpawn.stream().filter(s -> s.getCoordinate().y() < 25).toList();
                    salmonMatchesInSpawnExceptLast.stream().forEach(s -> {
                    s.setCoordinate(new Coordinate(s.getCoordinate().x(), s.getCoordinate().y() + 1));
                    salmonMatchService.save(s);});
                }
                players.stream().forEach(p -> p.setEnergy(5));
                playerService.saveAll(players);
                match.setRound(round+1);
            }
        }

        else if(phase.equals(Phase.TILES)) {
            List<Integer> rds = List.of(17, 14, 11, 8, 5, 2, 0);
            if (rds.contains(mtNoC.size())) {
                players.stream().forEach(p -> p.setEnergy(5));
                playerService.saveAll(players);
                match.setPhase(Phase.MOVING);
                match.setActualPlayer(match.getInitialPlayer());
            }
            if (match.getRound() == 0 && mtNoC.size() != 17) this.changePlayerTurn(player);
        }

        else {
            if (playerService.checkPlayerNoEnergy(player)) {
                this.changePlayerTurn(player); 
            }
        }

        if (playerService.checkPlayerIsDead(playerId)) { 
            this.changePlayerTurn(player);
            playerService.setPlayerDead(player);
            
        } else if (playerService.checkPlayerFinished(playerId)) {
            playerService.setPlayerNoEnergy(player); 
            this.changePlayerTurn(player);
        }
        /**
         * Muere (sigue teniendo energia)
         * Muere (se queda justo sin energia)
         * Termina (sigue teniendo energia)
         * Termina (no tiene mas energia)
         */

        endRound(matchId);
        this.checkGameHasFinished(matchId);
        this.save(match);
        return matchMapper.toDTO(match);
    }

    public MatchDTO finalScore(Integer id) {
        List<Player> players = playerService.findPlayersByMatch(id);
        Match match = this.findById(id);
        
        if (match.getFinalScoreCalculated() == true) {
            throw new ConflictException("Final score for this match has already been calculated");
        }

        for (Player p : players) {
            User u = p.getUserPlayer();
            List<SalmonMatch> salmons = salmonMatchService.findSalmonsFromPlayerInSpawn(p.getId());
            u.setPlayedgames(u.getPlayedgames() + 1);
            if(!salmons.isEmpty()) {
                Integer totalPoints = 0;
                Integer salmonPoints = salmons.stream().mapToInt(s -> salmonMatchService.findPointsFromASalmonInSpawn(s.getId())).sum();
                Integer filePoints = salmons.stream().mapToInt(sM -> sM.getSalmonsNumber()).sum();
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
        this.save(match);
        return matchMapper.toDTO(match);
    }
    
    @Transactional
    public void checkGameHasFinished(Integer matchId) {
        Match match = matchRepository.findById(matchId).get();
        List<MatchTile> tiles = matchTileService.findByMatchId(matchId);
        List<SalmonMatch> salmons = salmonMatchService.findAllFromMatch(matchId);
        List<SalmonMatch> noCoord = salmonMatchService.findByMatchIdNoCoord(matchId);
        List<Player> players = playerService.findPlayersByMatch(matchId);
        if(tiles.isEmpty() || salmons.isEmpty() ||  (salmons.stream().filter(s -> s.getCoordinate() != null).allMatch(s -> s.getCoordinate().y() > 20) && noCoord.isEmpty()) ||  players.stream().allMatch(p -> p.getAlive() == false)
         || players.stream().allMatch(p -> playerService.checkPlayerFinished(p.getId()))) {
            match.setState(State.FINALIZED);
            save(match);
        }
    }
}