package es.us.dp1.l4_01_24_25.upstream.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ConflictException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.DTO.MatchDTO;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MTCoordinateDTO;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService.NextPlayerResult;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementService;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @InjectMocks
    private MatchService matchService;

    @Mock private MatchRepository matchRepository;
    @Mock private MatchMapper matchMapper;
    @Mock private PlayerService playerService;
    @Mock private UserService userService;
    @Mock private SalmonMatchService salmonMatchService;
    @Mock private UserAchievementService userAchievementService;
    @Mock private MatchTileService matchTileService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    private Match match;
    private Player player1;
    private Player player2;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1);
        user1.setName("User1");
        user1.setPlayedgames(0);
        user1.setVictories(0);
        user1.setTotalpoints(0);

        user2 = new User();
        user2.setId(2);
        user2.setName("User2");
        user2.setPlayedgames(0);
        user2.setVictories(0);
        user2.setTotalpoints(0);

        player1 = new Player();
        player1.setId(1);
        player1.setUserPlayer(user1);
        player1.setPlayerOrder(0);
        player1.setEnergy(5);
        player1.setAlive(true);
        player1.setPoints(0);

        player2 = new Player();
        player2.setId(2);
        player2.setUserPlayer(user2);
        player2.setPlayerOrder(1);
        player2.setEnergy(5);
        player2.setAlive(true);
        player2.setPoints(0);

        match = new Match();
        match.setId(1);
        match.setName("Test Match");
        match.setState(State.WAITING);
        match.setRound(0);
        match.setPhase(Phase.TILES);
        match.setActualPlayer(player1);
        match.setInitialPlayer(player1);
        match.setFinalScoreCalculated(false);

        player1.setMatch(match);
        player2.setMatch(match);
    }

    // ── Helpers reutilizables ─────────────────────────────────────────────────
    private void stubChangePhaseBase(Phase phase, int round) {
        match.setPhase(phase);
        match.setRound(round);
        match.setActualPlayer(player1);
        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(playerService.findById(1)).thenReturn(player1);
        when(playerService.playersForChangePhase(1)).thenReturn(List.of(player1, player2));
    }

    private void stubEndRound(List<MatchTile> tiles, List<SalmonMatch> salmons) {
        when(matchTileService.findByMatchId(1)).thenReturn(tiles);
        when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(Collections.emptyList());
        when(salmonMatchService.findAllFromMatch(1)).thenReturn(salmons);
        when(playerService.findPlayersByMatch(1)).thenReturn(List.of(player1, player2));
    }

    private List<MTCoordinateDTO> createDummyTilesList(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> new MTCoordinateDTO(i, new Coordinate(null, null)))
                .collect(Collectors.toList());
    }

    // ── createMatchWMatchCreator ──────────────────────────────────────────────

    @Test
    void shouldCreateMatchWithNameAndPassword() {
        when(userService.findById(1)).thenReturn(user1);
        when(matchRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(matchMapper.toDTO(any())).thenReturn(new MatchDTO());

        MatchDTO result = matchService.createMatchWMatchCreator(1,
                Map.of("name", "Nueva partida", "password", "1234"));

        assertThat(result).isNotNull();
        verify(matchRepository).save(any(Match.class));
    }

    // ── startGame ─────────────────────────────────────────────────────────────

    @Test
    void shouldStartGameAndSetOnGoingState() {
        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(playerService.findPlayersByMatch(1)).thenReturn(List.of(player1, player2));

        matchService.startGame(1);

        // Verificamos estado en lugar de interacciones
        assertThat(match.getState()).isEqualTo(State.ON_GOING);
        assertThat(match.getActualPlayer()).isEqualTo(player1);
        assertThat(match.getInitialPlayer()).isEqualTo(player1);
        assertThat(match.getPlayersNumber()).isEqualTo(2);
    }

    @Test
    void shouldThrowWhenStartingNonExistentMatch() {
        when(matchRepository.findById(999)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> matchService.startGame(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── changeInitialPlayer ───────────────────────────────────────────────────

    @Test
    void shouldRotateInitialPlayerToNext() {
        match.setInitialPlayer(player1);
        when(playerService.findAlivePlayersByMatchSortedPlayerOrder(1))
                .thenReturn(List.of(player1, player2));

        matchService.changeInitialPlayer(match);

        assertThat(match.getInitialPlayer()).isEqualTo(player2);
    }

    // ── changePhase — RAMAS FALTANTES (Happy Paths) ───────────────────────────

    @Test
    void shouldChangeTurnDuringTilesPhaseRoundZero() {
        // Simula: Fase TILES, Ronda 0, el jugador coloca una loseta, quedan 16 pendientes.
        // Resultado esperado: Pasa el turno al jugador 2, se mantiene en fase TILES y ronda 0.
        stubChangePhaseBase(Phase.TILES, 0);
        
        // Quedan 16 losetas (ni 17, ni 0, ni múltiplos de recarga)
        List<MTCoordinateDTO> tilesLeft = createDummyTilesList(16);
        when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(tilesLeft);
        
        // Mocks para el cambio de turno
        when(playerService.findAlivePlayersByMatchSortedPlayerOrder(1)).thenReturn(List.of(player1, player2));
        when(playerService.findNextPlayer(player1, List.of(player1, player2)))
                .thenReturn(new NextPlayerResult(player2, 1));
        
        // Mocks para endRound (sin condiciones especiales en ronda 0)
        when(matchTileService.findByMatchId(1)).thenReturn(List.of(new MatchTile()));
        when(salmonMatchService.findAllFromMatch(1)).thenReturn(List.of(new SalmonMatch()));
        when(playerService.findPlayersByMatch(1)).thenReturn(List.of(player1, player2));

        matchService.changePhase(1, 1);

        // Verificación de estado
        assertThat(match.getActualPlayer()).isEqualTo(player2);
        assertThat(match.getPhase()).isEqualTo(Phase.TILES);
        assertThat(match.getRound()).isEqualTo(0);
    }

    @Test
    void shouldNotChangeTurnDuringMovingPhaseIfEnergyRemains() {
        // Simula: Fase MOVING, Ronda 1, el jugador se mueve pero le quedan 4 energías.
        // Resultado esperado: Mantiene el turno el mismo jugador.
        stubChangePhaseBase(Phase.MOVING, 1);
        player1.setEnergy(4); // Aún tiene energía
        
        // En MOVING ya no hay losetas pendientes
        when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(Collections.emptyList());
        when(playerService.checkPlayerNoEnergy(player1)).thenReturn(false); // Tiene energía
        when(playerService.checkPlayerIsDead(1)).thenReturn(false);
        
        stubEndRound(List.of(new MatchTile()), List.of(new SalmonMatch()));

        matchService.changePhase(1, 1);

        // Verificación de estado: El jugador 1 sigue siendo el jugador actual
        assertThat(match.getActualPlayer()).isEqualTo(player1);
        assertThat(match.getPhase()).isEqualTo(Phase.MOVING);
    }

    // ── changePhase — turno ───────────────────────────────────────────────────

    @Test
    void shouldThrowWhenNotPlayersTurn() {
        match.setActualPlayer(player2);
        when(matchRepository.findById(1)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchService.changePhase(1, 1))
                .isInstanceOf(NotValidMoveException.class)
                .hasMessageContaining("It is not your turn!");
    }

    // ── changePhase — fase TILES sin losetas pendientes ───────────────────────

    @Test
    void shouldChangeToMovingAndRechargeWhenNoTilesLeft() {
        stubChangePhaseBase(Phase.TILES, 1);
        when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1))
                .thenReturn(Collections.emptyList());
        stubEndRound(List.of(new MatchTile()), List.of(new SalmonMatch()));

        matchService.changePhase(1, 1);

        // Comprobamos el ESTADO modificado
        assertThat(match.getPhase()).isEqualTo(Phase.MOVING);
        assertThat(player1.getEnergy()).isEqualTo(5);
        assertThat(player2.getEnergy()).isEqualTo(5);
        assertThat(match.getActualPlayer()).isEqualTo(player1);
    }

    // ── changePhase — fase MOVING sin energía ────────────────────────────────

    @Test
    void shouldChangeTurnWhenPlayerHasNoEnergy() {
        player1.setEnergy(0);
        stubChangePhaseBase(Phase.MOVING, 1);
        when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(Collections.emptyList());
        when(playerService.checkPlayerNoEnergy(player1)).thenReturn(true);
        when(playerService.findAlivePlayersByMatchSortedPlayerOrder(1)).thenReturn(List.of(player1, player2));
        when(playerService.findNextPlayer(player1, List.of(player1, player2)))
                .thenReturn(new NextPlayerResult(player2, 1));
        
        stubEndRound(List.of(new MatchTile()), List.of(new SalmonMatch()));

        matchService.changePhase(1, 1);

        assertThat(match.getActualPlayer()).isEqualTo(player2);
    }

    // ── changePhase — jugador muerto ──────────────────────────────────────────

    @Test
    void shouldChangeTurnWhenPlayerDies() {
        stubChangePhaseBase(Phase.MOVING, 1);
        when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(Collections.emptyList());
        when(playerService.checkPlayerIsDead(1)).thenReturn(true); // El jugador ha muerto
        
        // Mocks de cambio de turno
        when(playerService.findAlivePlayersByMatchSortedPlayerOrder(1)).thenReturn(List.of(player2));
        when(playerService.findNextPlayer(player1, List.of(player2)))
                .thenReturn(new NextPlayerResult(player2, 0));
                
        stubEndRound(List.of(new MatchTile()), List.of(new SalmonMatch()));

        matchService.changePhase(1, 1);

        // Verificamos el estado principal
        assertThat(match.getActualPlayer()).isEqualTo(player2);
        // Aquí mantenemos este verify porque setPlayerDead está delegado en otro servicio
        verify(playerService).setPlayerDead(player1);
    }

    // ── checkGameHasFinished ──────────────────────────────────────────────────

    @ParameterizedTest(name = "tiles vacías={0}, salmones vacíos={1}, todos muertos={2}, todos terminados={3} -> {4}")
    @CsvSource({
        "true,  false, false, false, FINALIZED",
        "false, true,  false, false, FINALIZED",
        "false, false, true,  false, FINALIZED",
        "false, false, false, true,  FINALIZED",
        "false, false, false, false, ON_GOING",
    })
    void shouldFinalizeMatchWhenEndConditionMet(
            boolean tilesEmpty, boolean salmonsEmpty,
            boolean allDead, boolean allFinished, State expectedState) {

        match.setState(State.ON_GOING);
        stubChangePhaseBase(Phase.MOVING, 3);
        when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(Collections.emptyList());

        when(matchTileService.findByMatchId(1))
                .thenReturn(tilesEmpty ? Collections.emptyList() : List.of(new MatchTile()));
        when(salmonMatchService.findAllFromMatch(1))
                .thenReturn(salmonsEmpty ? Collections.emptyList() : List.of(new SalmonMatch()));

        player1.setAlive(!allDead);
        player2.setAlive(!allDead);
        when(playerService.findPlayersByMatch(1)).thenReturn(List.of(player1, player2));

        lenient().when(playerService.checkPlayerFinished(any())).thenReturn(allFinished);
        matchService.changePhase(1, 1);

        // Verificación Pura de Estado
        assertThat(match.getState()).isEqualTo(expectedState);
    }

    // ── finalScore ────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "y={0}, salmonsNumber={1} -> expectedPoints={2}")
    @CsvSource({
        "21, 1, 2",  // (21-20) spawn pts + 1 salmon = 2
        "21, 2, 3",  // (21-20) + 2 = 3
        "25, 1, 6",  // (25-20) + 1 = 6
        "25, 2, 7",  // (25-20) + 2 = 7
        "20, 2, 2",  // 0 spawn pts + 2 salmons = 2
    })
    void shouldCalculateFinalScoreCorrectly(int yCoord, int numSalmons, int expectedPoints) {
        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(playerService.findPlayersByMatch(1)).thenReturn(List.of(player1));

        SalmonMatch sm = new SalmonMatch();
        sm.setId(1);
        sm.setSalmonsNumber(numSalmons);
        sm.setCoordinate(new Coordinate(1, yCoord));

        when(salmonMatchService.findSalmonsFromPlayerInSpawn(1)).thenReturn(List.of(sm));
        when(salmonMatchService.findPointsFromAllSalmonInSpawn(1))
                .thenReturn(yCoord > 20 ? yCoord - 20 : 0);
        when(matchRepository.save(match)).thenReturn(match);
        when(matchMapper.toDTO(match)).thenReturn(new MatchDTO());

        matchService.finalScore(1);

        // Comprobamos directamente los estados alterados en Jugador y Usuario
        assertThat(player1.getPoints()).isEqualTo(expectedPoints);
        assertThat(user1.getTotalpoints()).isEqualTo(expectedPoints);
        assertThat(user1.getPlayedgames()).isEqualTo(1);
    }

    @Test
    void shouldIncrementVictoriesToWinnerOnly() {
        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(playerService.findPlayersByMatch(1)).thenReturn(List.of(player1, player2));

        SalmonMatch sm1 = new SalmonMatch(); sm1.setId(1); sm1.setSalmonsNumber(2);
        sm1.setCoordinate(new Coordinate(1, 25));
        SalmonMatch sm2 = new SalmonMatch(); sm2.setId(2); sm2.setSalmonsNumber(1);
        sm2.setCoordinate(new Coordinate(1, 21));

        when(salmonMatchService.findSalmonsFromPlayerInSpawn(1)).thenReturn(List.of(sm1));
        when(salmonMatchService.findSalmonsFromPlayerInSpawn(2)).thenReturn(List.of(sm2));
        when(salmonMatchService.findPointsFromAllSalmonInSpawn(1)).thenReturn(5);
        when(salmonMatchService.findPointsFromAllSalmonInSpawn(2)).thenReturn(1);
        when(matchRepository.save(match)).thenReturn(match);
        when(matchMapper.toDTO(match)).thenReturn(new MatchDTO());

        matchService.finalScore(1);

        // player1 = 5+2=7, player2 = 1+1=2 → user1 gana
        assertThat(user1.getVictories()).isEqualTo(1);
        assertThat(user2.getVictories()).isEqualTo(0);
    }

    @Test
    void shouldThrowConflictWhenScoreAlreadyCalculated() {
        match.setFinalScoreCalculated(true);
        when(matchRepository.findById(1)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchService.finalScore(1))
                .isInstanceOf(ConflictException.class);
    }
}