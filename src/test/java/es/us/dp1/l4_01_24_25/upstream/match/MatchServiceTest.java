package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ConflictException;
import es.us.dp1.l4_01_24_25.upstream.match.matchDTO.MatchDTO;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementService;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @InjectMocks
    MatchService matchService;

    @Mock
    MatchRepository matchRepository;
    @Mock
    MatchMapper matchMapper;
    @Mock
    PlayerService playerService;
    @Mock
    UserService userService;
    @Mock
    SalmonMatchService salmonMatchService;
    @Mock
    UserAchievementService userAchievementService;
    @Mock
    MatchTileService matchTileService;

    @Test
    void findById_shouldReturnEntity() {
        Match match = new Match();
        match.setId(1);
        when(matchRepository.findById(1)).thenReturn(Optional.of(match));

        Match result = matchService.findById(1);

        assertEquals(1, result.getId());
    }

    @Test
    void findAll_shouldReturnList() {
        Match match = new Match();
        when(matchRepository.findAll()).thenReturn(List.of(match));

        List<Match> matches = matchService.findAll();

        assertEquals(1, matches.size());
    }

    @Test
    void save_shouldCallRepositorySave() {
        Match match = new Match();
        when(matchRepository.save(match)).thenReturn(match);

        Match result = matchService.save(match);

        assertEquals(match, result);
    }

    @Test
    void delete_shouldCallRepositoryDeleteById() {
        matchService.deleteById(1);
        verify(matchRepository).deleteById(1);
    }

    @Test
    void findDTOById_shouldReturnMatchDTO() {
        Match match = new Match();
        match.setId(1);
        MatchDTO dto = new MatchDTO();
        dto.setId(1);
        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(matchMapper.toDTO(match)).thenReturn(dto);

        MatchDTO result = matchService.findByIdAsDTO(1);
        assertEquals(1, result.getId());
    }

    @Test
    void saveAsDTO_shouldMapAndSave() {
        MatchDTO dto = new MatchDTO();
        Match match = new Match();
        Match saved = new Match();
        MatchDTO expected = new MatchDTO();

        when(matchMapper.toEntity(dto)).thenReturn(match);
        when(matchRepository.save(match)).thenReturn(saved);
        when(matchMapper.toDTO(saved)).thenReturn(expected);

        MatchDTO result = matchService.saveAsDTO(dto);

        assertEquals(expected, result);
    }

    @Test
    void update_shouldModifyFieldsAndSave() {
        Match match = new Match();
        match.setId(1);
        match.setPassword("pass1");

        Match existing = new Match();
        existing.setId(1);
        existing.setPassword("old");

        MatchDTO dto = new MatchDTO();
        dto.setId(1);
        dto.setPassword("pass1");

        when(matchMapper.toEntity(dto)).thenReturn(match);
        when(matchRepository.findById(1)).thenReturn(Optional.of(existing));
        when(matchRepository.save(existing)).thenReturn(existing);
        when(matchMapper.toDTO(existing)).thenReturn(dto);

        MatchDTO result = matchService.updateAsDTO(existing.getId(), dto);

        assertEquals("pass1", result.getPassword());
    }

    @ParameterizedTest
    @CsvSource({
        "0,1", "1,2", "5,6"
    })
    void updateRound_shouldIncreaseRound(int initial, int expected) {
        Match match = new Match();
        match.setId(1);
        match.setRound(initial);
        MatchDTO dto = new MatchDTO();
        dto.setRound(expected);

        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(matchRepository.save(any())).thenReturn(match);
        when(matchMapper.toDTO(match)).thenReturn(dto);

        MatchDTO result = matchService.updateRound(1);
        assertEquals(expected, result.getRound());
    }

    @Test
    void createMatchWMatchCreator_shouldReturnDTO() {
        Map<String, String> data = Map.of("name", "test", "password", "123");
        User user = new User();
        Match match = new Match();
        MatchDTO dto = new MatchDTO();

        when(userService.findById(1)).thenReturn(user);
        when(matchRepository.save(any())).thenReturn(match);
        when(matchMapper.toDTO(match)).thenReturn(dto);

        MatchDTO result = matchService.createMatchWMatchCreator(1, data);
        assertNotNull(result);
    }

    @Test
    void startGame_shouldReturnStartedGame() {
        Match match = new Match();
        match.setId(1);
        Player p = new Player();
        List<Player> players = List.of(p);
        MatchDTO dto = new MatchDTO();

        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(playerService.findPlayersByMatch(1)).thenReturn(players);
        when(matchRepository.save(match)).thenReturn(match);
        when(matchMapper.toDTO(match)).thenReturn(dto);

        MatchDTO result = matchService.startGame(1);
        assertNotNull(result);
    }

    @Test
    void findHeronWithCoordsFromGame_shouldReturnList() {
        List<MatchTile> tiles = List.of(new MatchTile());
        when(matchTileService.findHeronWithCoordsFromGame(1)).thenReturn(tiles);

        List<MatchTile> result = matchTileService.findHeronWithCoordsFromGame(1);
        assertEquals(1, result.size());
    }

    @Test
    void createMatchWMatchCreator_shouldCreateMatchWithDefaultValues() {
        User user = new User();
        user.setId(1);

        Map<String, String> body = Map.of("name", "Partida1", "password", "abc");

        Match savedMatch = new Match();
        savedMatch.setId(1);
        savedMatch.setName("Partida1");
        savedMatch.setState(State.WAITING);

        MatchDTO dto = new MatchDTO();
        dto.setId(1);
        dto.setName("Partida1");

        when(userService.findById(1)).thenReturn(user);
        when(matchRepository.save(any(Match.class))).thenReturn(savedMatch);
        when(matchMapper.toDTO(any(Match.class))).thenReturn(dto);

        MatchDTO result = matchService.createMatchWMatchCreator(1, body);

        assertEquals("Partida1", result.getName());
        assertEquals(1, result.getId());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 1",
        "1, 2",
        "5, 6"
    })
    void updateRound_shouldIncrementRoundCorrectly(int initialRound, int expectedRound) {
        Match match = new Match();
        match.setId(1);
        match.setRound(initialRound);

        Match saved = new Match();
        saved.setId(1);
        saved.setRound(expectedRound);

        MatchDTO dto = new MatchDTO();
        dto.setId(1);
        dto.setRound(expectedRound);

        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(Match.class))).thenReturn(saved);
        when(matchMapper.toDTO(any(Match.class))).thenReturn(dto);

        MatchDTO result = matchService.updateRound(1);

        assertEquals(expectedRound, result.getRound());
    }

    @Test
    void startGame_shouldSetInitialAndActualPlayer() {
        Match match = new Match();
        match.setId(1);
        match.setState(State.WAITING);
        match.setPlayersNumber(0);

        Player p = new Player();
        p.setId(1);
        p.setPlayerOrder(0);

        List<Player> players = List.of(p);

        MatchDTO dto = new MatchDTO();
        dto.setId(1);
        dto.setState(State.ON_GOING);
        dto.setPlayersNumber(1);

        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(playerService.findPlayersByMatch(1)).thenReturn(players);
        when(matchRepository.save(any(Match.class))).thenReturn(match);
        when(matchMapper.toDTO(any(Match.class))).thenReturn(dto);

        MatchDTO result = matchService.startGame(1);

        assertEquals(State.ON_GOING, result.getState());
        assertEquals(1, result.getPlayersNumber());
    }

    @Test
    void finalScore_shouldThrowIfAlreadyCalculated() {
        Match match = new Match();
        match.setId(1);
        match.setFinalScoreCalculated(true);

        when(matchRepository.findById(1)).thenReturn(Optional.of(match));

        assertThrows(ConflictException.class, () -> matchService.finalScore(1));
    }

    @Test
    void finalScore_shouldCalculateScoreAndSetFinalScoreFlag() {
        Match match = new Match();
        match.setId(1);
        match.setFinalScoreCalculated(false);

        Player player = new Player();
        player.setId(1);
        player.setPoints(0);
        User user = new User();
        user.setId(1);
        user.setPlayedgames(0);
        user.setTotalpoints(0);
        player.setUserPlayer(user);

        SalmonMatch salmon = new SalmonMatch();
        salmon.setId(1);
        salmon.setSalmonsNumber(2);
        Coordinate coord = new Coordinate(1, 1);
        salmon.setCoordinate(coord);

        List<Player> players = List.of(player);
        List<SalmonMatch> salmons = List.of(salmon);

        when(playerService.findPlayersByMatch(1)).thenReturn(players);
        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(salmonMatchService.findSalmonsFromPlayerInSpawn(1)).thenReturn(salmons);
        when(salmonMatchService.findPointsFromAllSalmonInSpawn(1)).thenReturn(10);
        when(matchRepository.save(any())).thenReturn(match);
        when(matchMapper.toDTO(any())).thenReturn(new MatchDTO());

        MatchDTO result = matchService.finalScore(1);

        assertNotNull(result);
        verify(userService).save(user);
        verify(playerService).save(player);
    }

    @Test
    void changeInitialPlayer_shouldRotateToNextPlayer() {
        Match match = new Match();
        match.setId(1);

        Player p1 = new Player();
        p1.setPlayerOrder(0);

        Player p2 = new Player();
        p2.setPlayerOrder(1);

        match.setInitialPlayer(p1);

        List<Player> players = List.of(p1, p2);

        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(playerService.findAlivePlayersByMatch(1)).thenReturn(players);
        when(matchRepository.save(any())).thenReturn(match);

        //matchService.changeInitialPlayer(match);

        assertEquals(p2, match.getInitialPlayer());
    }
}