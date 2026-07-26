package es.us.dp1.l4_01_24_25.upstream.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ConflictException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.DTO.DashboardMatchDTO;
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
@DisplayName("Match Service Tests")
class MatchServiceTest {

    @Spy
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

    // ==========================================
    // HELPER STUB METHODS
    // ==========================================

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
                .mapToObj(i -> new MTCoordinateDTO(i, new Coordinate(null, null))).toList();
    }

    // ==========================================
    // NESTED TEST CLASSES
    // ==========================================

    @Nested
    @DisplayName("1. Match Creation & Startup")
    class MatchCreationAndStartupTests {

        @Test
        @DisplayName("Should successfully create a match with given name and password")
        void shouldCreateMatchWithNameAndPassword() {
            when(userService.findById(1)).thenReturn(user1);
            when(matchRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
            when(matchMapper.toDTO(any())).thenReturn(new MatchDTO());

            MatchDTO result = matchService.createMatchWMatchCreator(1,
                    Map.of("name", "New match", "password", "1234"));

            assertThat(result).isNotNull();
            verify(matchRepository).save(any(Match.class));
        }

        @Test
        @DisplayName("Should change match state to ON_GOING when starting the game")
        void shouldStartGameAndSetOnGoingState() {
            when(matchRepository.findById(1)).thenReturn(Optional.of(match));
            when(playerService.findPlayersByMatch(1)).thenReturn(List.of(player1, player2));

            matchService.startGame(1);

            // We verify state rather than raw interactions
            assertThat(match.getState()).isEqualTo(State.ON_GOING);
            assertThat(match.getActualPlayer()).isEqualTo(player1);
            assertThat(match.getInitialPlayer()).isEqualTo(player1);
            assertThat(match.getPlayersNumber()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should throw exception if trying to start a non-existent match")
        void shouldThrowWhenStartingNonExistentMatch() {
            when(matchRepository.findById(999)).thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> matchService.startGame(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("2. Turn & Phase Management")
    class TurnAndPhaseManagementTests {

        @Test
        @DisplayName("Should throw exception if a player tries to play out of turn")
        void shouldThrowWhenNotPlayersTurn() {
            match.setActualPlayer(player2);
            when(matchRepository.findById(1)).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.changePhase(1, 1))
                    .isInstanceOf(NotValidMoveException.class)
                    .hasMessageContaining("It is not your turn!");
        }

        @Test
        @DisplayName("Should successfully rotate the initial player to the next one")
        void shouldRotateInitialPlayerToNext() {
            match.setInitialPlayer(player1);
            when(playerService.findAlivePlayersByMatchSortedPlayerOrder(1))
                    .thenReturn(List.of(player1, player2));

            matchService.changeInitialPlayer(match);

            assertThat(match.getInitialPlayer()).isEqualTo(player2);
        }

        @Test
        @DisplayName("Tiles phase (Round 0): Should rotate turn but keep Phase and Round intact")
        void shouldChangeTurnDuringTilesPhaseRoundZero() {
            // Simulates: TILES phase, Round 0, player places a tile, 16 remaining.
            // Expected outcome: Passes turn to player 2, remains in TILES phase and round 0.
            stubChangePhaseBase(Phase.TILES, 0);
            
            // 16 tiles left (not 17, not 0, not a recharge multiple)
            List<MTCoordinateDTO> tilesLeft = createDummyTilesList(16);
            when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(tilesLeft);
            
            // Turn change mocks
            when(playerService.findAlivePlayersByMatchSortedPlayerOrder(1)).thenReturn(List.of(player1, player2));
            when(playerService.findNextPlayer(player1, List.of(player1, player2)))
                    .thenReturn(new NextPlayerResult(player2, 1));
            
            // EndRound mocks (no special conditions triggered in round 0)
            when(matchTileService.findByMatchId(1)).thenReturn(List.of(new MatchTile()));
            when(salmonMatchService.findAllFromMatch(1)).thenReturn(List.of(new SalmonMatch()));
            when(playerService.findPlayersByMatch(1)).thenReturn(List.of(player1, player2));

            matchService.changePhase(1, 1);

            // State verification
            assertThat(match.getActualPlayer()).isEqualTo(player2);
            assertThat(match.getPhase()).isEqualTo(Phase.TILES);
            assertThat(match.getRound()).isEqualTo(0);
        }

        @Test
        @DisplayName("Moving phase: Should NOT change turn if current player still has energy")
        void shouldNotChangeTurnDuringMovingPhaseIfEnergyRemains() {
            // Simulates: MOVING phase, Round 1. Player moves but has 4 energy points left.
            // Expected outcome: Turn is not passed. Same player keeps playing.
            stubChangePhaseBase(Phase.MOVING, 1);
            player1.setEnergy(4); // Still has energy
            
            // In MOVING phase, there are no unplaced tiles
            when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(Collections.emptyList());
            when(playerService.checkPlayerNoEnergy(player1)).thenReturn(false); // Has energy
            when(playerService.checkPlayerIsDead(1)).thenReturn(false);
            
            stubEndRound(List.of(new MatchTile()), List.of(new SalmonMatch()));

            matchService.changePhase(1, 1);

            // State verification: Player 1 remains the actual player
            assertThat(match.getActualPlayer()).isEqualTo(player1);
            assertThat(match.getPhase()).isEqualTo(Phase.MOVING);
        }

        @Test
        @DisplayName("Tiles phase: Should transition to Moving Phase and recharge energy when out of tiles")
        void shouldChangeToMovingAndRechargeWhenNoTilesLeft() {
            stubChangePhaseBase(Phase.TILES, 1);
            when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1))
                    .thenReturn(Collections.emptyList());
            stubEndRound(List.of(new MatchTile()), List.of(new SalmonMatch()));

            matchService.changePhase(1, 1);

            // Verify modified STATE
            assertThat(match.getPhase()).isEqualTo(Phase.MOVING);
            assertThat(player1.getEnergy()).isEqualTo(5);
            assertThat(player2.getEnergy()).isEqualTo(5);
            assertThat(match.getActualPlayer()).isEqualTo(player1);
        }

        @Test
        @DisplayName("Moving phase: Should rotate turn when player completely depletes energy")
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

        @Test
        @DisplayName("Should immediately pass turn and update status if a player dies mid-game")
        void shouldChangeTurnWhenPlayerDies() {
            stubChangePhaseBase(Phase.MOVING, 1);
            when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(Collections.emptyList());
            when(playerService.checkPlayerIsDead(1)).thenReturn(true); // Player is dead
            
            // Turn change mocks
            when(playerService.findAlivePlayersByMatchSortedPlayerOrder(1)).thenReturn(List.of(player2));
            when(playerService.findNextPlayer(player1, List.of(player2)))
                    .thenReturn(new NextPlayerResult(player2, 0));
                    
            stubEndRound(List.of(new MatchTile()), List.of(new SalmonMatch()));

            matchService.changePhase(1, 1);

            // Main state check
            assertThat(match.getActualPlayer()).isEqualTo(player2);
            // We keep this verify because setPlayerDead logic is delegated to the PlayerService
            verify(playerService).setPlayerDead(player1);
        }
    }

    @Nested
    @DisplayName("3. End Round Mechanics (Board Evolution)")
    class EndRoundMechanicsTests {

        /*
         * CONTEXT FOR NEW DEVELOPERS:
         * The endRound() method in MatchService manages how the board evolves depending on the round.
         * These tests verify the 3 main milestones of a match's lifecycle:
         * 1. Round 2: Initial ocean cleanup.
         * 2. Mid-Game rounds: River current simulation (elements flow down towards y=0).
         * 3. Late-Game rounds (>7): Cleanup of outdated/left-behind tiles.
         */

        @Test
        @DisplayName("Round 2 (Early-Game): Deletes salmons that failed to leave the ocean")
        void shouldDeleteSalmonsInOceanOnRoundTwo() {
            // Simulates: MOVING phase, ending Round 2.
            stubChangePhaseBase(Phase.MOVING, 2);
            when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(Collections.emptyList());

            // Prepare entities: One salmon left behind in the ocean and another safe in the river
            SalmonMatch salmonInOcean = new SalmonMatch(); 
            salmonInOcean.setCoordinate(null); // Represents being in the ocean
            
            SalmonMatch salmonInRiver = new SalmonMatch(); 
            salmonInRiver.setCoordinate(new Coordinate(1, 1)); // Already safe in the river

            stubEndRound(Collections.emptyList(), List.of(salmonInOcean, salmonInRiver));

            matchService.changePhase(1, 1);

            // Verify that batch delete is called ONLY with the ocean salmons
            verify(salmonMatchService).deleteAllInBatch(List.of(salmonInOcean));
        }

        @Test
        @DisplayName("Mid-Game: Moves river current down and recharges players energy")
        void shouldMoveRiverAndRechargeEnergyDuringMidGameEndRound() {
            // Simulates: TILES phase, Round 3.
            // The milestone of 14 tiles left triggers energy recharge and midGame movement.
            stubChangePhaseBase(Phase.TILES, 3);
            List<MTCoordinateDTO> tilesLeft = createDummyTilesList(14); // 14 is in the milestone list
            when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(tilesLeft);

            // Prepare entities: some should move down (y=3, y=5) and others fall off the waterfall (y=0)
            MatchTile tileToMove = new MatchTile(); tileToMove.setCoordinate(new Coordinate(1, 5));
            MatchTile tileToDelete = new MatchTile(); tileToDelete.setCoordinate(new Coordinate(1, 0));
            
            SalmonMatch salmonToMove = new SalmonMatch(); salmonToMove.setCoordinate(new Coordinate(1, 3));
            SalmonMatch salmonToDelete = new SalmonMatch(); salmonToDelete.setCoordinate(new Coordinate(1, 0));

            stubEndRound(List.of(tileToMove, tileToDelete), List.of(salmonToMove, salmonToDelete));

            matchService.changePhase(1, 1);

            // Energy is recharged and phase changes to MOVING
            assertThat(player1.getEnergy()).isEqualTo(5);
            assertThat(player2.getEnergy()).isEqualTo(5);
            assertThat(match.getPhase()).isEqualTo(Phase.MOVING);
            assertThat(tileToMove.getCoordinate()).isEqualTo(new Coordinate(1, 5));
            assertThat(salmonToMove.getCoordinate()).isEqualTo(new Coordinate(1, 3));
        }

        @Test
        @DisplayName("Late-Game (>7): Deletes outdated tiles and salmons out of the game range")
        void shouldDeleteOutdatedTilesAndSalmonsWhenRoundGreaterThanSeven() {
            // Simulates: MOVING phase, Round 9. 
            // From round 7 onwards, bottom board collapses. Deletion threshold is y=(9-8)=1
            stubChangePhaseBase(Phase.MOVING, 9); 
            when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(Collections.emptyList());

            // Outdated entities at y=1
            MatchTile oldTile = new MatchTile(); oldTile.setId(10); oldTile.setCoordinate(new Coordinate(1, 1));
            SalmonMatch oldSalmon = new SalmonMatch(); oldSalmon.setId(20); oldSalmon.setCoordinate(new Coordinate(1, 1));

            // Valid entities at y=2
            MatchTile validTile = new MatchTile(); validTile.setId(11); validTile.setCoordinate(new Coordinate(1, 2));
            SalmonMatch validSalmon = new SalmonMatch(); validSalmon.setId(21); validSalmon.setCoordinate(new Coordinate(1, 2));

            stubEndRound(List.of(oldTile, validTile), List.of(oldSalmon, validSalmon));

            matchService.changePhase(1, 1);

            verify(matchTileService).deleteById(10); // Deleted outdated tile
            verify(salmonMatchService).deleteAllInBatch(List.of(oldSalmon)); // Deleted outdated salmon
        }
    }

    @Nested
    @DisplayName("4. End Game & Scoring")
    class EndGameAndScoringTests {

        @ParameterizedTest(name = "tiles empty={0}, salmons empty={1}, all dead={2}, all finished={3} -> expected {4}")
        @CsvSource({
            "true,  false, false, false, FINALIZED",
            "false, true,  false, false, FINALIZED",
            "false, false, true,  false, FINALIZED",
            "false, false, false, true,  FINALIZED",
            "false, false, false, false, ON_GOING",
        })
        @DisplayName("Should detect endgame conditions correctly and finalize match")
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

            // Pure State Verification
            assertThat(match.getState()).isEqualTo(expectedState);
        }

        @ParameterizedTest(name = "y={0}, salmonsNumber={1} -> expectedPoints={2}")
        @CsvSource({
            "21, 1, 2",  // (21-20) spawn pts + 1 salmon = 2
            "21, 2, 3",  // (21-20) + 2 = 3
            "25, 1, 6",  // (25-20) + 1 = 6
            "25, 2, 7",  // (25-20) + 2 = 7
            "20, 2, 2",  // 0 spawn pts + 2 salmons = 2
        })
        @DisplayName("Should calculate points correctly based on salmon amount and Y position")
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

            // Verify directly against altered states on Player and User entities
            assertThat(player1.getPoints()).isEqualTo(expectedPoints);
            assertThat(user1.getTotalpoints()).isEqualTo(expectedPoints);
            assertThat(user1.getPlayedgames()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should award victory count only to the winner")
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

            // player1 = 5+2=7, player2 = 1+1=2 → user1 wins
            assertThat(user1.getVictories()).isEqualTo(1);
            assertThat(user2.getVictories()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw ConflictException if the match was already calculated")
        void shouldThrowConflictWhenScoreAlreadyCalculated() {
            match.setFinalScoreCalculated(true);
            when(matchRepository.findById(1)).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.finalScore(1))
                    .isInstanceOf(ConflictException.class);
        }
    }

    @Test
    void testUpdateEntityFields() {
        Match newMatch = new Match();
        newMatch.setPassword("secret");
        newMatch.setState(State.ON_GOING);
        newMatch.setPlayersNumber(3);
        newMatch.setRound(2);
        newMatch.setPhase(Phase.MOVING);
        newMatch.setFinalScoreCalculated(true);
        newMatch.setInitialPlayer(player1);
        newMatch.setActualPlayer(player2);
        newMatch.setMatchCreator(user1);

        Match result = matchService.updateEntityFields(newMatch, match);

        assertThat(result.getPassword()).isEqualTo("secret");
        assertThat(result.getState()).isEqualTo(State.ON_GOING);
        assertThat(result.getPlayersNumber()).isEqualTo(3);
        assertThat(result.getRound()).isEqualTo(2);
        assertThat(result.getPhase()).isEqualTo(Phase.MOVING);
        assertThat(result.getFinalScoreCalculated()).isTrue();
        assertThat(result.getInitialPlayer()).isEqualTo(player1);
        assertThat(result.getActualPlayer()).isEqualTo(player2);
        assertThat(result.getMatchCreator()).isEqualTo(user1);
    }

    @Test
    void testNotificationMethods() {
        MatchDTO matchDTO = new MatchDTO();
        matchDTO.setId(1);
        matchService.notifyMatchUpdateDTO(matchDTO);
        verify(messagingTemplate).convertAndSend("/topic/match/update/1", matchDTO);

        matchService.notifyMatchStarts(1);
        verify(messagingTemplate).convertAndSend("/topic/match/start/1", "Navigate to game screen");

        SalmonMatch sm = new SalmonMatch();
        sm.setMatch(match);
        MatchTile mt = new MatchTile();
        when(matchTileService.toDTOList(List.of(mt))).thenReturn(List.of());
        when(salmonMatchService.toDTOList(List.of(sm))).thenReturn(List.of());

        matchService.notifyTilesAndSalmons(List.of(sm), List.of(mt));
        verify(messagingTemplate).convertAndSend(eq("/topic/tilesAndSalmons/1"), any(Map.class));

        matchService.notifyPlayerAndMatchUpdate(match);
        verify(playerService).notifyPlayersUpdate(1);
    }

    @Test
    void testFindAllAsDashboardDTOAndCreateMatch() {
        DashboardMatchDTO dashDTO = new DashboardMatchDTO(1, "m1", "pass", State.WAITING, 2);
        when(matchRepository.findAllAsDashboardDTO()).thenReturn(List.of(dashDTO));
        assertThat(matchService.findAllAsDashboardDTO()).containsExactly(dashDTO);

        when(userService.findById(1)).thenReturn(user1);
        MatchDTO dto = new MatchDTO();
        when(matchMapper.toDTO(any())).thenReturn(dto);

        MatchDTO created = matchService.createMatchWMatchCreator(1, Map.of("name", "NewMatch", "password", "pass"));
        assertThat(created).isEqualTo(dto);
        verify(matchRepository).save(any(Match.class));
    }

    @Test
    void testStartGame() {
        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(playerService.findPlayersByMatch(1)).thenReturn(List.of(player1, player2));

        matchService.startGame(1);

        assertThat(match.getState()).isEqualTo(State.ON_GOING);
        assertThat(match.getActualPlayer()).isEqualTo(player1);
        assertThat(match.getInitialPlayer()).isEqualTo(player1);
        assertThat(match.getPlayersNumber()).isEqualTo(2);
        verify(matchRepository).save(match);
    }

    @Test
    void testChangePhase_NoMoreTilesToPlaceRechargeEnergy() {
        match.setPhase(Phase.TILES);
        match.setActualPlayer(player1);
        match.setRound(3);

        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(playerService.findById(1)).thenReturn(player1);
        when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(List.of()); // size == 0
        when(playerService.playersForChangePhase(1)).thenReturn(List.of(player1, player2));

        matchService.changePhase(1, 1);

        assertThat(match.getPhase()).isEqualTo(Phase.MOVING);
        assertThat(match.getActualPlayer()).isEqualTo(player1);
        assertThat(player1.getEnergy()).isEqualTo(5);
        verify(playerService).saveAll(List.of(player1, player2));
    }

    @Test
    void testChangePhase_NoMoreTilesToPlacePlayers_WithSpawnSalmons() {
        match.setPhase(Phase.MOVING);
        match.setActualPlayer(player1);
        match.setRound(1);
        player1.setEnergy(0);
        player2.setEnergy(0);

        when(matchRepository.findById(1)).thenReturn(Optional.of(match));
        when(playerService.findById(1)).thenReturn(player1);
        when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(List.of()); // size == 0
        when(playerService.playersForChangePhase(1)).thenReturn(List.of(player1, player2));
        when(playerService.checkPlayerNoEnergy(player1)).thenReturn(true);

        SalmonMatch smSpawn = new SalmonMatch();
        smSpawn.setId(10);
        smSpawn.setCoordinate(new Coordinate(1, 22));
        when(salmonMatchService.findFromGameInSpawn(1)).thenReturn(List.of(smSpawn));
        when(playerService.findAlivePlayersByMatchSortedPlayerOrder(1)).thenReturn(List.of(player1, player2));
        lenient().when(playerService.findNextPlayer(player1, List.of(player1, player2))).thenReturn(new NextPlayerResult(player2, 1));

        matchService.changePhase(1, 1);

        assertThat(smSpawn.getCoordinate().y()).isEqualTo(23);
        verify(salmonMatchService).saveAll(List.of(smSpawn));
        verify(salmonMatchService).notifySalmonMatchSpawnUpdate(List.of(smSpawn));
    }

    @Test
    void testEndRound_MidGameExecution() {
        match.setRound(3);
        match.setPhase(Phase.TILES);

        MTCoordinateDTO dtoNoC = new MTCoordinateDTO(1, null);
        List<MTCoordinateDTO> mtNoC = IntStream.range(0, 17).mapToObj(i -> dtoNoC).toList();
        when(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(mtNoC);

        MatchTile mtY0 = new MatchTile();
        mtY0.setId(1);
        mtY0.setCoordinate(new Coordinate(1, 0));

        MatchTile mtY1 = new MatchTile();
        mtY1.setId(2);
        mtY1.setCoordinate(new Coordinate(1, 1));

        when(matchTileService.findByMatchId(1)).thenReturn(List.of(mtY0, mtY1));

        SalmonMatch smY0 = new SalmonMatch();
        smY0.setId(10);
        smY0.setMatch(match);
        smY0.setCoordinate(new Coordinate(1, 0));

        SalmonMatch smY1 = new SalmonMatch();
        smY1.setId(11);
        smY1.setMatch(match);
        smY1.setCoordinate(new Coordinate(1, 1));

        when(salmonMatchService.findAllFromMatch(1)).thenReturn(List.of(smY0, smY1));
        when(playerService.findPlayersByMatch(1)).thenReturn(List.of(player1));

        org.springframework.test.util.ReflectionTestUtils.invokeMethod(matchService, "endRound", match);

        verify(salmonMatchService).delete(smY0);
        assertThat(smY1.getCoordinate().y()).isZero();
        verify(matchTileService).delete(mtY0);
        assertThat(mtY1.getCoordinate().y()).isZero();
        verify(matchService).notifyTilesAndSalmonsByMatchId(1);
    }
}


