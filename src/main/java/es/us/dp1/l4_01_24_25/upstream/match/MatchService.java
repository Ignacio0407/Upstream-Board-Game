package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ConflictException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.model.BaseServiceWithDTO;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService.NextPlayerResult;
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

    public MatchDTO startGame(Integer matchId) throws ResourceNotFoundException {
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
    public void changeInitialPlayer(Match match) {
        List<Player> players = playerService.findAlivePlayersByMatchSortedPlayerOrder(match.getId());
        Integer initialOrder = match.getInitialPlayer().getPlayerOrder();
        int currentIndex = players.indexOf(players.stream().filter(player -> player.getPlayerOrder().equals(initialOrder))
        .findFirst().get());
        int nextIndex = (currentIndex + 1) % players.size();
        Player nextInitialPlayer = players.get(nextIndex);
        match.setInitialPlayer(nextInitialPlayer);
        matchRepository.save(match); 
    }
    
    private Player findNextEligiblePlayer(Player currentPlayer, List<Player> players, List<Player> playersFinished, int currentIndex, Match match) {
        int nPlayers = players.size();
        Player nextPlayer = players.get(currentIndex);
        while (playersFinished.contains(nextPlayer)) {
            currentIndex = (currentIndex + 1) % nPlayers;
            nextPlayer = players.get(currentIndex);
            if (nextPlayer.equals(currentPlayer) &&
                salmonMatchService.findAllFromPlayer(currentPlayer.getId()).stream().allMatch(s -> s.getCoordinate().y() > 20)) {
                match.setState(State.FINALIZED);
                break;
            }
        }
        return nextPlayer;
    }
    private void handleEarlyPhase(Match match, Player player, List<Player> players, List<MatchTile> tilesNoCoord) {
        if (players.stream().allMatch(p -> p.getEnergy() <= 0)) {
            if (!tilesNoCoord.isEmpty()) match.setPhase(Phase.TILES);
            match.setRound(match.getRound() + 1);
            this.changeInitialPlayer(match);
            match.setActualPlayer(match.getInitialPlayer());
        } else {
            Player nextPlayer = playerService.findNextPlayer(player, players).player();
            match.setActualPlayer(nextPlayer);
        }
    }

    @Transactional
    private void changePlayerTurn(Player player) {
        Match match = player.getMatch();
        List<Player> players = playerService.findAlivePlayersByMatchSortedPlayerOrder(match.getId());
        List<MatchTile> tilesNoCoord = matchTileService.findByMatchIdNoCoord(match.getId());
        if (match.getRound() > 6) {
            List<Player> playersFinished = playerService.findAlivePlayersByMatch(match.getId()).stream().filter(
                p -> salmonMatchService.findAllFromPlayer(p.getId()).stream().allMatch(s -> s.getCoordinate().y() > 20)).toList();
            NextPlayerResult nxt = playerService.findNextPlayer(player, players);
            Player nextPlayer = this.findNextEligiblePlayer(player, players, playersFinished, nxt.nextIndex(), match);
            match.setActualPlayer(nextPlayer);
        } else {
            this.handleEarlyPhase(match, player, players, tilesNoCoord);
        }
        this.save(match);
    }

    @Transactional
    private void checkGameHasFinished (Match match) {
        List<MatchTile> tiles = matchTileService.findByMatchId(match.getId());
        List<SalmonMatch> salmons = salmonMatchService.findAllFromMatch(match.getId());
        List<Player> players = playerService.findPlayersByMatch(match.getId());
        if (tiles.isEmpty() || salmons.isEmpty() || players.stream().allMatch(p -> p.getAlive() == false) 
            || players.stream().allMatch(p -> playerService.checkPlayerFinished(p.getId()))) {
            match.setState(State.FINALIZED);
            this.save(match);
        }
    }

    private void noMoreTilesToPlaceRechargeEnergy (List<Player> players, Match match) {
        players.stream().forEach(p -> p.setEnergy(5));
            playerService.saveAll(players);
            match.setPhase(Phase.MOVING);
            match.setActualPlayer(match.getInitialPlayer());
    }
    private void noMoreTilesToPlacePlayers (Match match, Player player, List<Player> players, Integer round) {
        match.setPhase(Phase.MOVING);
            if(playerService.checkPlayerNoEnergy(player)){
                this.changePlayerTurn(player);
            }
            if(players.stream().allMatch(p -> p.getEnergy() <= 0)) {
                List<SalmonMatch> salmonMatchesInSpawn = salmonMatchService.findFromGameInSpawn(match.getId());
                if(!salmonMatchesInSpawn.isEmpty() && salmonMatchesInSpawn != null) {
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

    public MatchDTO changePhase(Integer matchId, Integer playerId) {
        Match match = this.findById(matchId);
        Player player = playerService.findById(playerId);
        Phase phase = match.getPhase();
        List<MatchTile> mtNoC = matchTileService.findByMatchIdNoCoord(matchId);
        List<Player> players = playerService.playersForChangePhase(matchId);
        Integer round = match.getRound();
        if (mtNoC.size() == 0 && phase == Phase.TILES) {
            this.noMoreTilesToPlaceRechargeEnergy(players, match);
        }
        else if (mtNoC.size() == 0) {
            this.noMoreTilesToPlacePlayers(match, player, players, round);
        }
        else if (phase.equals(Phase.TILES)) {
            List<Integer> rds = List.of(17, 14, 11, 8, 5, 2, 0);
            if (rds.contains(mtNoC.size()))
                this.noMoreTilesToPlaceRechargeEnergy(players, match);
            if (match.getRound() == 0 && mtNoC.size() != 17) this.changePlayerTurn(player);
        }
        else
            if (playerService.checkPlayerNoEnergy(player)) this.changePlayerTurn(player); 

        if (playerService.checkPlayerIsDead(playerId)) { 
            this.changePlayerTurn(player);
            playerService.setPlayerDead(player);
            
        } else if (playerService.checkPlayerFinished(playerId)) {
            playerService.setPlayerNoEnergy(player); 
            this.changePlayerTurn(player);
        }
        this.endRound(match);
        this.save(match);
        if (match.round >= 3)
            this.checkGameHasFinished(match);
        return matchMapper.toDTO(match);
    }

    private void roundEquals2 (Integer round, List<SalmonMatch> salmonMatches, List<Player> players) {
            List<SalmonMatch> mtInOcean = salmonMatches.stream().filter(m -> m.getCoordinate()==null).toList();
            for (SalmonMatch sm: mtInOcean) { 
                salmonMatchService.delete(sm.getId());
            }
            for(Player p : players) {
                Boolean dead = playerService.checkPlayerIsDead(p.getId());
                Boolean finished = playerService.checkPlayerFinished(p.getId());
                if (dead) playerService.setPlayerDead(p);
                if (finished) playerService.setPlayerNoEnergy(p);
            }
    }
    private void noMoreTilesToPlace (Integer round, List<SalmonMatch> salmonMatches, List<Player> players, List<MatchTile> mt) {
        List<SalmonMatch> mtOutOfPosition = salmonMatches.stream().filter(m -> m.getCoordinate().y() == (round - 8)).toList();
            for (SalmonMatch sm:mtOutOfPosition) {
                salmonMatchService.delete(sm.getId());
                playerService.checkPlayerIsDead(sm.getPlayer().getId());
            } 
            for (MatchTile m:mt) {
                if(m.getCoordinate().y() == (round - 8)){
                    matchTileService.delete(m.getId());
                }
            }
            for (Player p : players) {
                Boolean dead = playerService.checkPlayerIsDead(p.getId());
                Boolean finished = playerService.checkPlayerFinished(p.getId());
                if (dead) playerService.setPlayerDead(p);
                if (finished) playerService.setPlayerNoEnergy(p);
            }
    }
    private void midGame (Integer round, List<SalmonMatch> salmonMatches, List<Player> players, List<MatchTile> mt) {
        for(SalmonMatch sm:salmonMatches) {
                if (sm.getCoordinate().y()==0) salmonMatchService.delete(sm.getId());
                else sm.setCoordinate(new Coordinate(sm.getCoordinate().x(), sm.getCoordinate().y()-1));
            }
            salmonMatchService.saveAll(salmonMatches);

        for(MatchTile m:mt) {
            if(m.getCoordinate() != null && m.getCoordinate().y()==0) matchTileService.delete(m.getId());
            else if (m.getCoordinate() != null) m.setCoordinate(new Coordinate(m.getCoordinate().x(), m.getCoordinate().y()-1));
        }
        matchTileService.saveAll(mt);

        for(Player p : players) {
            Boolean dead = playerService.checkPlayerIsDead(p.getId());
            Boolean finished = playerService.checkPlayerFinished(p.getId());
            if (dead) playerService.setPlayerDead(p);
            if (finished) playerService.setPlayerNoEnergy(p);
        }
    }

    private Match endRound (Match match) throws ResourceNotFoundException {
        List<MatchTile> mt = matchTileService.findByMatchId(match.getId());
        List<MatchTile> mtNoc = matchTileService.findByMatchIdNoCoord(match.getId());
        List<SalmonMatch> salmonMatches = salmonMatchService.findAllFromMatch(match.getId());
        List<Player> players = playerService.findPlayersByMatch(match.getId());
        List<Integer> tilesPerRound = List.of(17, 14, 11, 8, 5, 2);
        Integer round = match.getRound();
        if (round == 2) 
            this.roundEquals2(round, salmonMatches, players);
        else if (round > 7 && mtNoc.size() == 0) 
            this.noMoreTilesToPlace(round, salmonMatches, players, mt);
        else if (round>2 && tilesPerRound.contains(mtNoc.size()) && match.getPhase() == Phase.TILES)
            this.midGame(round, salmonMatches, players, mt);
        return match;
    }

    public MatchDTO finalScore(Integer id) {
        List<Player> players = playerService.findPlayersByMatch(id);
        Match match = this.findById(id);
        if (match.getFinalScoreCalculated() == true) {
            throw new ConflictException("Final score for this match has already been calculated");
        }
        this.calculateFinalScore(players);
        this.findWinner(players);
        match.setFinalScoreCalculated(true);
        return matchMapper.toDTO(this.save(match));
    }

    private void findWinner (List<Player> players) {
        Optional<Player> winner = players.stream().max(Comparator.comparing(Player::getPoints));
        if(winner.isPresent()) {
            User winnerUser = winner.get().getUserPlayer();
            winnerUser.setVictories(winnerUser.getVictories() + 1);
            userService.save(winnerUser);
        }
    }
    private void calculateFinalScore (List<Player> players) {
        List<User> updatedUsers = new ArrayList<>();
        for (Player p : players) {
            User u = p.getUserPlayer();
            List<SalmonMatch> salmons = salmonMatchService.findSalmonsFromPlayerInSpawn(p.getId());
            u.setPlayedgames(u.getPlayedgames() + 1);
            if(!salmons.isEmpty()) {
                Integer salmonPoints = salmons.stream().mapToInt(s -> salmonMatchService.findPointsFromASalmonInSpawn(s.getId())).sum();
                Integer filePoints = salmons.stream().mapToInt(sM -> sM.getSalmonsNumber()).sum();
                Integer totalPoints = salmonPoints + filePoints;
                p.setPoints(totalPoints);
                u.setTotalpoints(u.getTotalpoints() + totalPoints);
                updatedUsers.add(u);
            }
            userAchievementService.checkAndUnlockAchievements(u.getId());   
        }
        playerService.saveAll(players);
        userService.saveAll(updatedUsers);
    }
    
}