package upstream.match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import upstream.coordinate.Coordinate;
import upstream.exceptions.ConflictException;
import upstream.exceptions.NotValidMoveException;
import upstream.exceptions.ResourceNotFoundException;
import upstream.match.dto.CreateMatchRequest;
import upstream.match.dto.DashboardMatchDTO;
import upstream.match.dto.MatchDTO;
import upstream.matchtile.MatchTile;
import upstream.matchtile.MatchTileService;
import upstream.matchtile.dto.MTCoordinateDTO;
import upstream.matchtile.dto.MatchTileDTO;
import upstream.model.BaseServiceWithDTO;
import upstream.player.Player;
import upstream.player.PlayerService;
import upstream.player.PlayerService.NextPlayerResult;
import upstream.salmonmatch.SalmonMatch;
import upstream.salmonmatch.SalmonMatchService;
import upstream.salmonmatch.dto.SalmonMatchDTO;
import upstream.user.User;
import upstream.user.UserService;
import upstream.userachievement.UserAchievementService;

@Service
public class MatchService extends BaseServiceWithDTO<Match, MatchDTO, Integer>{

    MatchRepository matchRepository;
    MatchMapper matchMapper;
    PlayerService playerService;
    UserService userService;
    SalmonMatchService salmonMatchService;
    UserAchievementService userAchievementService;
    MatchTileService matchTileService;
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MatchService(MatchRepository matchRepository, MatchMapper matchMapper, @Lazy PlayerService playerService, @Lazy UserService userService, @Lazy SalmonMatchService salmonMatchService, @Lazy UserAchievementService userAchievementService, @Lazy MatchTileService matchTileService, SimpMessagingTemplate messagingTemplate) {
        super(matchRepository, matchMapper);
        this.matchRepository = matchRepository;
        this.matchMapper = matchMapper;
        this.playerService = playerService;
        this.userService = userService;
        this.salmonMatchService = salmonMatchService;
        this.userAchievementService = userAchievementService;
        this.matchTileService = matchTileService;
        this.messagingTemplate = messagingTemplate;
    }

    private static final String SALMON_MATCHES = "salmonMatches";
    private static final String MATCH_TILES = "matchTiles";

    @Override
    @Transactional
    protected Match updateEntityFields(Match newMatch, Match matchToUpdate) {
        matchToUpdate.setPassword(newMatch.getPassword());
        matchToUpdate.setState(newMatch.getState());
        matchToUpdate.setPlayersNumber(newMatch.getPlayersNumber());
        matchToUpdate.setRound(newMatch.getRound());
        matchToUpdate.setPhase(newMatch.getPhase());
        matchToUpdate.setFinalScoreCalculated(newMatch.getFinalScoreCalculated());
        matchToUpdate.setInitialPlayer(newMatch.getInitialPlayer());
        matchToUpdate.setActualPlayer(newMatch.getActualPlayer());
        matchToUpdate.setMatchCreator(newMatch.getMatchCreator());
        return matchToUpdate;
    }

    public DashboardMatchDTO toDashboardDTO(Match match) {
        return new DashboardMatchDTO(match.getId(), match.getName(), match.getPassword(), match.getState(), match.getPlayersNumber());
    }

    public List<DashboardMatchDTO> findAllAsDashboardDTO() {
        return this.matchRepository.findAllAsDashboardDTO();
    }

    public void notifyMatchUpdate (Match match) {
        this.messagingTemplate.convertAndSend("/topic/match/update/" + match.getId(), this.matchMapper.toDTO(match));
    }

    public void notifyMatchUpdateDTO (MatchDTO matchDTO) {
        this.messagingTemplate.convertAndSend("/topic/match/update/" + matchDTO.getId(), matchDTO);
    }

    public void notifyMatchUpdateDashboard (Match match) {
        this.messagingTemplate.convertAndSend("/topic/matches/dashboard", this.toDashboardDTO(match));
    }

    public void notifyMatchStarts (Integer matchId) {
        this.messagingTemplate.convertAndSend("/topic/match/start/" + matchId, "Navigate to game screen");
    }

    public void notifyTilesAndSalmonsByMatchId (Integer matchId) {
        List<SalmonMatchDTO> salmonMatches = this.salmonMatchService.findAllFromMatchDTO(matchId);
        List<MatchTileDTO> matchTiles = this.matchTileService.findByMatchIdAsDTO(matchId);
        this.messagingTemplate.convertAndSend("/topic/tilesAndSalmonsByMatchId/" + matchId, Map.of(MATCH_TILES, matchTiles, SALMON_MATCHES, salmonMatches));
    }

    public void notifyTilesAndSalmons (List<SalmonMatch> salmonMatches, List<MatchTile> matchTiles) {
        this.messagingTemplate.convertAndSend("/topic/tilesAndSalmons/" + salmonMatches.get(0).getMatch().getId(),
        Map.of(MATCH_TILES, this.matchTileService.toDTOList(matchTiles), SALMON_MATCHES, this.salmonMatchService.toDTOList(salmonMatches)));
    }

    public void notifyPlayerAndMatchUpdate (Match match) {
        this.save(match);
        this.playerService.notifyPlayersUpdate(match.getId());
        this.notifyMatchUpdate(match);
    }

    public MatchDTO createMatchWMatchCreator(Integer userId, CreateMatchRequest request) {
        User u = this.userService.findById(userId);
        String name = request.name() != null ? request.name().trim() : "";
        String password = request.password() != null ? request.password().trim() : "";
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
        this.notifyMatchUpdateDashboard(m);
        return this.matchMapper.toDTO(m);
    }

    public MatchDTO createMatchWMatchCreator(Integer userId, Map<String, String> requestBody) {
        String name = requestBody != null ? requestBody.getOrDefault("name", "") : "";
        String password = requestBody != null ? requestBody.getOrDefault("password", "") : "";
        return createMatchWMatchCreator(userId, new CreateMatchRequest(name, password));
    }

    public void startGame(Integer matchId) throws ResourceNotFoundException {
        Match match = this.findById(matchId);
        List<Player> player = this.playerService.findPlayersByMatch(matchId);
        match.setState(State.ON_GOING);
        match.setActualPlayer(player.get(0));
        match.setInitialPlayer(player.get(0));
        match.setPlayersNumber(player.size());
        this.save(match);
        this.notifyMatchUpdate(match);
        this.notifyMatchUpdateDashboard(match);
    }

    @Transactional
    public void changeInitialPlayer(Match match) {
        List<Player> players = this.playerService.findAlivePlayersByMatchSortedPlayerOrder(match.getId());
        Integer initialOrder = match.getInitialPlayer().getPlayerOrder();
        Optional<Player> initialPlayerOpt = players.stream().filter(player -> player.getPlayerOrder().equals(initialOrder)).findFirst();
        int currentIndex = initialPlayerOpt.isPresent() ? players.indexOf(initialPlayerOpt.get()) : 0;
        int nextIndex = (currentIndex + 1) % players.size();
        Player nextInitialPlayer = players.get(nextIndex);
        match.setInitialPlayer(nextInitialPlayer);
        this.save(match);
    }

    private Player findNextEligiblePlayer(Player currentPlayer, List<Player> players, List<Player> playersFinished, int currentIndex, Match match) {
        int nPlayers = players.size();
        Player nextPlayer = players.get(currentIndex);
        while (playersFinished.contains(nextPlayer)) {
            currentIndex = (currentIndex + 1) % nPlayers;
            nextPlayer = players.get(currentIndex);
            if (nextPlayer.equals(currentPlayer) &&
                this.salmonMatchService.findByPlayerIdAsCoordinateDTO(currentPlayer.getId()).stream().allMatch(s -> s.getCoordinate().y() > 20) &&
                !match.getState().equals(State.FINALIZED)) {
                match.setState(State.FINALIZED);
                this.finalScore(match.getId());
                break;
            }
        }
        return nextPlayer;
    }
    private void handleEarlyPhase(Match match, Player player, List<Player> players, List<MTCoordinateDTO> tilesNoCoord) {
        if (players.stream().allMatch(p -> p.getEnergy() <= 0)) {
            if (!tilesNoCoord.isEmpty()) match.setPhase(Phase.TILES);
            match.setRound(match.getRound() + 1);
            this.changeInitialPlayer(match);
            match.setActualPlayer(match.getInitialPlayer());
        } else {
            Player nextPlayer = this.playerService.findNextPlayer(player, players).player();
            match.setActualPlayer(nextPlayer);
        }
    }

    private void changePlayerTurn(Player player) {
        Match match = player.getMatch();
        List<Player> players = this.playerService.findAlivePlayersByMatchSortedPlayerOrder(match.getId());
        List<MTCoordinateDTO> tilesNoCoord = new ArrayList<>();
        if (match.getRound() <= 6) tilesNoCoord = this.matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(match.getId());
        if (match.getRound() > 6) {
            List<Player> playersFinished = this.playerService.findAlivePlayersByMatch(match.getId()).stream().filter(
                p -> this.salmonMatchService.findByPlayerIdAsCoordinateDTO(p.getId()).stream().allMatch(s -> s.getCoordinate().y() > 20)).toList();
            NextPlayerResult nxt = this.playerService.findNextPlayer(player, players);
            Player nextPlayer = this.findNextEligiblePlayer(player, players, playersFinished, nxt.nextIndex(), match);
            match.setActualPlayer(nextPlayer);
        } else {
            this.handleEarlyPhase(match, player, players, tilesNoCoord);
        }
        this.save(match);
    }

    private void checkGameHasFinished (Match match, Triple data) {
        List<MatchTile> tiles = data.matchTiles;
        List<SalmonMatch> salmons = data.salmonMatches;
        List<Player> players = data.players;
        if ((tiles.isEmpty() || salmons.isEmpty() || players.stream().allMatch(p -> Boolean.FALSE.equals(p.getAlive()))
            || players.stream().allMatch(p -> this.playerService.checkPlayerFinished(p.getId())))
            && !match.getState().equals(State.FINALIZED)) {
            match.setState(State.FINALIZED);
            this.save(match);
            this.finalScore(match.getId());
            this.notifyMatchUpdate(match);
        }
    }

    private void noMoreTilesToPlaceRechargeEnergy (List<Player> players, Match match) {
        players.forEach(p -> p.setEnergy(5));
        this.playerService.saveAll(players);
        match.setPhase(Phase.MOVING);
        match.setActualPlayer(match.getInitialPlayer());
        this.save(match);
    }
    private void noMoreTilesToPlacePlayers (Match match, Player player, List<Player> players) {
        match.setPhase(Phase.MOVING);
        if(Boolean.TRUE.equals(this.playerService.checkPlayerNoEnergy(player))){
            this.changePlayerTurn(player);
        }
        if(players.stream().allMatch(p -> p.getEnergy() <= 0)) {
            List<SalmonMatch> salmonMatchesInSpawn = this.salmonMatchService.findFromGameInSpawn(match.getId());
            if(!salmonMatchesInSpawn.isEmpty() && salmonMatchesInSpawn != null) {
                List<SalmonMatch> salmonMatchesInSpawnExceptLast = salmonMatchesInSpawn.stream().filter(s -> s.getCoordinate().y() < 25).toList();
                salmonMatchesInSpawnExceptLast.forEach(s -> s.setCoordinate(new Coordinate(s.getCoordinate().x(), s.getCoordinate().y() + 1)));
                this.salmonMatchService.saveAll(salmonMatchesInSpawnExceptLast);
                this.salmonMatchService.notifySalmonMatchSpawnUpdate(salmonMatchesInSpawnExceptLast);
            }
            players.forEach(p -> p.setEnergy(5));
            this.playerService.saveAll(players);
            match.setRound(match.getRound()+1);
        }
        this.save(match);
    }

    public void changePhase(Integer matchId, Integer playerId) {
        Match match = this.findById(matchId);
        if (!match.getActualPlayer().getId().equals(playerId))
            throw new NotValidMoveException("It is not your turn!");
        Player player = this.playerService.findById(playerId);
        Phase phase = match.getPhase();
        List<MTCoordinateDTO> mtNoC = new ArrayList<>();
        if (match.getRound() <= 6) mtNoC = this.matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(matchId);
        List<Player> players = this.playerService.playersForChangePhase(matchId);
        if (mtNoC.isEmpty() && phase == Phase.TILES) {
            this.noMoreTilesToPlaceRechargeEnergy(players, match);
        }
        else if (mtNoC.isEmpty()) {
            this.noMoreTilesToPlacePlayers(match, player, players);
        }
        else if (phase.equals(Phase.TILES)) {
            List<Integer> coordinatesToPlaceEachRound = List.of(17, 14, 11, 8, 5, 2, 0);
            if (coordinatesToPlaceEachRound.contains(mtNoC.size()))
                this.noMoreTilesToPlaceRechargeEnergy(players, match);
            if (match.getRound() == 0 && mtNoC.size() != 17) this.changePlayerTurn(player);
        }
        else
            if (Boolean.TRUE.equals(this.playerService.checkPlayerNoEnergy(player))) this.changePlayerTurn(player);

        if (match.getRound() >= 1 && this.playerService.checkPlayerIsDead(playerId)) {
            this.playerService.setPlayerDead(player);
            this.changePlayerTurn(player);
        }
        else if (match.getRound() >= 6 && this.playerService.checkPlayerFinished(playerId)) {
            this.playerService.setPlayerNoEnergy(player);
            this.changePlayerTurn(player);
        }
        Triple data = this.endRound(match);
        if (match.getRound() >= 2)
            this.checkGameHasFinished(match, data);
        this.playerService.notifyPlayersUpdate(matchId);
        this.notifyMatchUpdate(match);
    }

    private void roundEquals2 (List<SalmonMatch> salmonMatches, List<Player> players) {
        List<SalmonMatch> salmonMatchesInOcean = salmonMatches.stream().filter(m -> m.getCoordinate()==null).toList();
        this.salmonMatchService.deleteAllInBatch(salmonMatchesInOcean);
        this.playerService.checkPlayersDeadOrFinished(players);
    }
    private void noMoreTilesToPlace (Integer round, List<SalmonMatch> salmonMatches, List<Player> players, List<MatchTile> mt) {
        List<SalmonMatch> salmonMatchesOutOfPosition = salmonMatches.stream().filter(m -> m.getCoordinate().y() == (round - 8)).toList();
        this.salmonMatchService.deleteAllInBatch(salmonMatchesOutOfPosition);
        List<MatchTile> matchTilesToDelete = mt.stream().filter(m -> m.getCoordinate().y() == round - 8).toList();
        this.matchTileService.deleteAllInBatch(matchTilesToDelete);
        this.playerService.checkPlayersDeadOrFinished(players);
        this.notifyTilesAndSalmonsByMatchId(players.get(0).getMatch().getId());
    }
    private void midGame (List<SalmonMatch> salmonMatches, List<Player> players, List<MatchTile> mt) {
        List<SalmonMatch> salmonMatchesToDelete = new ArrayList<>();
        List<MatchTile> matchTilesToDelete = new ArrayList<>();
        for(SalmonMatch sm:salmonMatches) {
            if (sm.getCoordinate().y()==0) salmonMatchesToDelete.add(sm);
            else {
                sm.setCoordinate(new Coordinate(sm.getCoordinate().x(), sm.getCoordinate().y()-1));
            }
        }
        this.salmonMatchService.deleteAllInBatch(salmonMatchesToDelete);
        for(MatchTile m:mt) {
            if(m.getCoordinate() != null && m.getCoordinate().y()==0) matchTilesToDelete.add(m);
            else if (m.getCoordinate() != null) {
                m.setCoordinate(new Coordinate(m.getCoordinate().x(), m.getCoordinate().y()-1));
            }
        }
        this.matchTileService.deleteAllInBatch(matchTilesToDelete);
        this.playerService.checkPlayersDeadOrFinished(players);
        this.notifyTilesAndSalmonsByMatchId(players.get(0).getMatch().getId());
    }

    private record Triple (List<MatchTile> matchTiles, List<SalmonMatch> salmonMatches, List<Player> players) {};

    private Triple endRound (Match match) throws ResourceNotFoundException {
        List<MatchTile> mt = this.matchTileService.findByMatchId(match.getId());
        List<MTCoordinateDTO> mtNoc = this.matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(match.getId());
        List<SalmonMatch> salmonMatches = this.salmonMatchService.findAllFromMatch(match.getId());
        List<Player> players = this.playerService.findPlayersByMatch(match.getId());
        List<Integer> tilesPerRound = List.of(17, 14, 11, 8, 5, 2);
        Integer round = match.getRound();
        if (round == 2) {
            this.roundEquals2(salmonMatches, players);
        }
        else if (round > 7 && mtNoc.isEmpty()) {
            this.noMoreTilesToPlace(round, salmonMatches, players, mt);
        }
        else if (round>2 && tilesPerRound.contains(mtNoc.size()) && match.getPhase() == Phase.TILES) {
            this.midGame(salmonMatches, players, mt);
        }
        this.save(match);
        return new Triple(mt, salmonMatches, players);
    }

    public MatchDTO finalScore(Integer id) {
        List<Player> players = this.playerService.findPlayersByMatch(id);
        Match match = this.findById(id);
        if (Boolean.TRUE.equals(match.getFinalScoreCalculated())) {
            throw new ConflictException("Final score for this match has already been calculated");
        }
        this.calculateFinalScore(players);
        this.findWinner(players);
        match.setFinalScoreCalculated(true);
        return this.matchMapper.toDTO(this.save(match));
    }

    private void findWinner (List<Player> players) {
        if (players == null || players.isEmpty()) return;

        Match match = players.get(0).getMatch();
        Map<Integer, List<SalmonMatch>> playerSalmonsInSpawn = new HashMap<>();
        for (Player p : players) {
            playerSalmonsInSpawn.put(p.getId(), this.salmonMatchService.findSalmonsFromPlayerInSpawn(p.getId()));
        }

        Comparator<Player> winnerComparator = (p1, p2) -> {
            // 1. Highest total points
            int points1 = p1.getPoints() != null ? p1.getPoints() : 0;
            int points2 = p2.getPoints() != null ? p2.getPoints() : 0;
            if (points1 != points2) {
                return Integer.compare(points1, points2);
            }

            List<SalmonMatch> salmons1 = playerSalmonsInSpawn.getOrDefault(p1.getId(), Collections.emptyList());
            List<SalmonMatch> salmons2 = playerSalmonsInSpawn.getOrDefault(p2.getId(), Collections.emptyList());

            // 2. Tie-breaker 1: Highest number of surviving salmons in spawn
            int totalSalmons1 = salmons1.stream().mapToInt(s -> s.getSalmonsNumber() != null ? s.getSalmonsNumber() : 0).sum();
            int totalSalmons2 = salmons2.stream().mapToInt(s -> s.getSalmonsNumber() != null ? s.getSalmonsNumber() : 0).sum();
            if (totalSalmons1 != totalSalmons2) {
                return Integer.compare(totalSalmons1, totalSalmons2);
            }

            // 3. Tie-breaker 2: Highest number of salmon tokens in spawn
            int tokenCount1 = salmons1.size();
            int tokenCount2 = salmons2.size();
            if (tokenCount1 != tokenCount2) {
                return Integer.compare(tokenCount1, tokenCount2);
            }

            // 4. Tie-breaker 3: Highest advanced pieces (comparing Y coordinates ordered from highest to lowest)
            List<Integer> coords1 = salmons1.stream()
                .map(s -> (s.getCoordinate() != null && s.getCoordinate().y() != null) ? s.getCoordinate().y() : 0)
                .sorted(Comparator.reverseOrder())
                .toList();
            List<Integer> coords2 = salmons2.stream()
                .map(s -> (s.getCoordinate() != null && s.getCoordinate().y() != null) ? s.getCoordinate().y() : 0)
                .sorted(Comparator.reverseOrder())
                .toList();

            for (int i = 0; i < Math.min(coords1.size(), coords2.size()); i++) {
                int cmp = Integer.compare(coords1.get(i), coords2.get(i));
                if (cmp != 0) {
                    return cmp;
                }
            }

            // 5. Tie-breaker 4 (in 2-player games): The player without the initial player marker wins
            if (match != null && match.getPlayersNumber() != null && match.getPlayersNumber() == 2 && match.getInitialPlayer() != null) {
                boolean isInitial1 = p1.getId().equals(match.getInitialPlayer().getId());
                boolean isInitial2 = p2.getId().equals(match.getInitialPlayer().getId());
                if (isInitial1 != isInitial2) {
                    return Boolean.compare(!isInitial1, !isInitial2);
                }
            }

            return 0;
        };

        Optional<Player> winner = players.stream().max(winnerComparator);
        if(winner.isPresent()) {
            User winnerUser = winner.get().getUserPlayer();
            if (winnerUser != null) {
                winnerUser.setVictories((winnerUser.getVictories() != null ? winnerUser.getVictories() : 0) + 1);
                this.userService.save(winnerUser);
            }
        }
    }
    private void calculateFinalScore (List<Player> players) {
        List<User> updatedUsers = new ArrayList<>();
        for (Player p : players) {
            User u = p.getUserPlayer();
            List<SalmonMatch> salmons = this.salmonMatchService.findSalmonsFromPlayerInSpawn(p.getId());
            u.setPlayedgames(u.getPlayedgames() + 1);
            if(!salmons.isEmpty()) {
                Integer salmonPoints = salmons.stream().mapToInt(s -> this.salmonMatchService.findPointsFromAllSalmonInSpawn(s)).sum();
                Integer filePoints = salmons.stream().mapToInt(sM -> sM.getSalmonsNumber()).sum();
                Integer totalPoints = salmonPoints + filePoints;
                p.setPoints(totalPoints);
                u.setTotalpoints(u.getTotalpoints() + totalPoints);
                updatedUsers.add(u);
            }
            this.userAchievementService.checkAndUnlockAchievements(u.getId());
        }
        this.playerService.saveAll(players);
        this.userService.saveAll(updatedUsers);
    }

}
