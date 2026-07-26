package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.InsufficientEnergyException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.match.Phase;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import es.us.dp1.l4_01_24_25.upstream.salmon.SalmonService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SalmonMatchDTO;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SMCoordinateDTO;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchService.MoveRequest;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileType;

@ExtendWith(MockitoExtension.class)
@DisplayName("Salmon Match Service Tests")
class SalmonMatchServiceTest {

    @InjectMocks
    private SalmonMatchService salmonMatchService;

    @Mock private SalmonMatchRepository salmonMatchRepository;
    @Mock private SalmonMatchMapper salmonMatchMapper;
    @Mock private PlayerService playerService;
    @Mock private SalmonService salmonService;
    @Mock private MatchTileService matchTileService;
    @Mock private MatchService matchService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    private Match match;
    private Player player;
    private SalmonMatch salmonMatch;
    private Salmon salmon;

    @BeforeEach
    void setUp() {
        match = new Match();
        match.setId(1);
        match.setPhase(Phase.MOVING);
        match.setPlayersNumber(2);
        match.setRound(6);

        player = new Player();
        player.setId(1);
        player.setEnergy(5);
        player.setMatch(match);

        match.setActualPlayer(player); // by default it IS this player's turn

        salmon = new Salmon();
        salmon.setId(1);
        salmon.setImage("salmon_2_red");

        salmonMatch = new SalmonMatch();
        salmonMatch.setId(1);
        salmonMatch.setPlayer(player);
        salmonMatch.setMatch(match);
        salmonMatch.setSalmonsNumber(2);
        salmonMatch.setSalmon(salmon);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MatchTile tile(int x, int y, TileType type, int orientation, int capacity, int salmonsNumber) {
        Tile t = new Tile();
        t.setType(type);
        MatchTile mt = new MatchTile();
        mt.setCoordinate(new Coordinate(x, y));
        mt.setTile(t);
        mt.setOrientation(orientation);
        mt.setCapacity(capacity);
        mt.setSalmonsNumber(salmonsNumber);
        mt.setMatch(match);
        return mt;
    }

    private MatchTile water(int x, int y) {
        return tile(x, y, TileType.WATER, 0, 10, 0);
    }

    private void stubBatch(List<SalmonMatch> mySalmons, List<MatchTile> tiles) {
        when(playerService.findMyPlayer(match.getId())).thenReturn(player);
        lenient().when(playerService.findPlayersByMatch(match.getId())).thenReturn(List.of(player));
        when(matchTileService.findByMatchId(match.getId())).thenReturn(tiles);
        when(salmonMatchRepository.findByPlayerId(player.getId())).thenReturn(mySalmons);
        lenient().when(matchTileService.findMyTile(any(), any())).thenAnswer(inv -> {
            Coordinate c = inv.getArgument(1);
            if (c == null) return null;
            return tiles.stream()
                    .filter(t -> t.getCoordinate() != null && t.getCoordinate().equals(c))
                    .findFirst().orElse(null);
        });
        lenient().when(salmonMatchRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(salmonMatchMapper.toDTOList(any())).thenReturn(Collections.emptyList());
    }

    private void stubBatch(SalmonMatch sm, List<MatchTile> tiles) {
        stubBatch(List.of(sm), tiles);
    }

    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Calculations: findPointsFromAllSalmonInSpawn")
    class FindPointsFromAllSalmonInSpawnTests {

        @ParameterizedTest(name = "Coordinate y={0} -> Grants {1} points")
        @CsvSource({"25, 5", "21, 1", "20, 0", "15, 0"})
        @DisplayName("Should correctly calculate spawn points based on Y coordinate")
        void shouldCalculateSpawnPoints(int y, int expectedPoints) {
            SalmonMatch sm = new SalmonMatch();
            sm.setCoordinate(new Coordinate(1, y));
            when(salmonMatchRepository.findById(1)).thenReturn(Optional.of(sm));

            assertThat(salmonMatchService.findPointsFromAllSalmonInSpawn(1)).isEqualTo(expectedPoints);
        }
    }

    @Nested
    @DisplayName("Batch Move: updateCoordinateBatch")
    class UpdateCoordinateBatchTests {

        @Nested
        @DisplayName("Entry Guards & Initial Checks")
        class EntryGuards {

            @Test
            @DisplayName("Should return immediately when moves are null or empty")
            void shouldReturnImmediatelyWhenMovesAreNullOrEmpty() {
                salmonMatchService.updateCoordinateBatch(1, null);
                salmonMatchService.updateCoordinateBatch(1, List.of());

                verifyNoInteractions(playerService, matchTileService, matchService, salmonMatchRepository);
            }

            @Test
            @DisplayName("Should throw exception when match phase is not MOVING")
            void shouldThrowWhenPhaseIsNotMoving() {
                match.setPhase(Phase.TILES);
                when(playerService.findMyPlayer(match.getId())).thenReturn(player);
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 0, false));

                assertThatThrownBy(() -> salmonMatchService.updateCoordinateBatch(1, moves))
                        .isInstanceOf(NotValidMoveException.class)
                        .hasMessageContaining("tiles have to be placed");
            }

            @Test
            @DisplayName("Should throw exception when it is not the player's turn")
            void shouldThrowWhenItIsNotThePlayersTurn() {
                Player otherPlayer = new Player();
                otherPlayer.setId(99);
                match.setActualPlayer(otherPlayer);
                when(playerService.findMyPlayer(match.getId())).thenReturn(player);
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 0, false));

                assertThatThrownBy(() -> salmonMatchService.updateCoordinateBatch(1, moves))
                        .isInstanceOf(NotValidMoveException.class)
                        .hasMessageContaining("It is not your turn!");
            }

            @Test
            @DisplayName("Should throw exception when salmon is not found or not owned by player")
            void shouldThrowWhenSalmonInMoveIsNotFoundOrNotOwned() {
                salmonMatch.setCoordinate(null);
                stubBatch(salmonMatch, List.of(water(1, 0)));
                List<MoveRequest> moves = List.of(new MoveRequest(999, 1, 0, false));

                assertThatThrownBy(() -> salmonMatchService.updateCoordinateBatch(1, moves))
                        .isInstanceOf(NotValidMoveException.class)
                        .hasMessageContaining("Salmon not found or not yours");
            }

            @Test
            @DisplayName("Should throw exception when moving to the same coordinate")
            void shouldThrowWhenMovingToSameCoordinate() {
                salmonMatch.setCoordinate(new Coordinate(1, 1));
                stubBatch(salmonMatch, List.of(water(1, 1)));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 1, false));

                assertThatThrownBy(() -> salmonMatchService.updateCoordinateBatch(1, moves))
                        .isInstanceOf(NotValidMoveException.class);
            }

            @Test
            @DisplayName("Should throw exception when player has no energy left")
            void shouldThrowWhenPlayerHasNoEnergy() {
                player.setEnergy(0);
                salmonMatch.setCoordinate(null);
                stubBatch(salmonMatch, List.of(water(1, 0)));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 0, false));

                assertThatThrownBy(() -> salmonMatchService.updateCoordinateBatch(1, moves))
                        .isInstanceOf(InsufficientEnergyException.class);
            }

            @Test
            @DisplayName("Should throw exception when advancing from sea to a Y != 0")
            void shouldThrowWhenFromSeaNotMovingToY0() {
                salmonMatch.setCoordinate(null);
                stubBatch(salmonMatch, List.of(water(1, 1))); 
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 1, false));

                assertThatThrownBy(() -> salmonMatchService.updateCoordinateBatch(1, moves))
                        .isInstanceOf(NotValidMoveException.class)
                        .hasMessageContaining("advance from 1 to 1");
            }

            @Test
            @DisplayName("Should throw exception when moving more than one step in the grid")
            void shouldThrowWhenMovingMoreThanOneStepInTheGrid() {
                salmonMatch.setCoordinate(new Coordinate(1, 1));
                stubBatch(salmonMatch, List.of(water(1, 1), water(1, 3)));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 3, false));

                assertThatThrownBy(() -> salmonMatchService.updateCoordinateBatch(1, moves))
                        .isInstanceOf(NotValidMoveException.class)
                        .hasMessageContaining("only move from 1 to 1");
            }
        }

        @Nested
        @DisplayName("Movement from Sea")
        class FromSea {

            @Test
            @DisplayName("Should move from sea to river with a standard cost of 1")
            void shouldMoveFromSeaToRiverWithCost1() {
                salmonMatch.setCoordinate(null);
                stubBatch(salmonMatch, List.of(water(1, 0)));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 0, false));

                salmonMatchService.updateCoordinateBatch(1, moves);

                assertThat(salmonMatch.getCoordinate()).isEqualTo(new Coordinate(1, 0));
                assertThat(player.getEnergy()).isEqualTo(4);
            }

            @Test
            @DisplayName("Should jump from sea with cost 3 when Y=0 tile is full")
            void shouldJumpFromSeaWhenY0TileFullAndNextNotFull() {
                salmonMatch.setCoordinate(null);
                MatchTile fullTile = tile(1, 0, TileType.WATER, 0, 2, 2);
                MatchTile nextTile = water(1, 1);
                stubBatch(salmonMatch, List.of(fullTile, nextTile));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 0, false));

                salmonMatchService.updateCoordinateBatch(1, moves);

                assertThat(salmonMatch.getCoordinate()).isEqualTo(new Coordinate(1, 1));
                assertThat(player.getEnergy()).isEqualTo(2); // cost 3
            }

            @Test
            @DisplayName("Should throw exception when both sea entry tiles are full")
            void shouldThrowWhenBothSeaTilesAreFull() {
                salmonMatch.setCoordinate(null);
                MatchTile fullTile = tile(1, 0, TileType.WATER, 0, 2, 2);
                MatchTile alsoFull = tile(1, 1, TileType.WATER, 0, 2, 2);
                stubBatch(salmonMatch, List.of(fullTile, alsoFull));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 0, false));

                assertThatThrownBy(() -> salmonMatchService.updateCoordinateBatch(1, moves))
                        .isInstanceOf(NotValidMoveException.class)
                        .hasMessageContaining("next tile is also full");
            }
        }

        @Nested
        @DisplayName("Grid Movement Matrix")
        class GridMovement {

            @ParameterizedTest(name = "Moves to ({0},{1}) | {2}({3}) -> {4}({5}) | energy={6}->{7}, salmons={8}->{9}")
            @CsvSource({
                "1, 2, WATER, 0, WATER, 0, 5, 4, 2, 2",
                "1, 2, WATER, 0, EAGLE, 0, 5, 4, 2, 1",
                "1, 2, WATER, 0, JUMP,  0, 5, 3, 2, 2",
                "1, 2, WATER, 0, BEAR,  0, 5, 3, 2, 1",
                "1, 2, BEAR,  3, WATER, 0, 5, 3, 2, 1",
                "1, 2, BEAR,  3, BEAR,  0, 5, 3, 2, 0",
                "0, 2, WATER, 0, WATER, 0, 5, 4, 2, 2",
                "0, 2, WATER, 0, JUMP,  4, 5, 3, 2, 2",
                "0, 2, WATER, 0, BEAR,  5, 5, 3, 2, 1",
                "0, 2, BEAR,  2, WATER, 0, 5, 3, 2, 1",
                "0, 2, BEAR,  2, BEAR,  5, 5, 3, 2, 0",
                "2, 2, WATER, 0, WATER, 0, 5, 4, 2, 2",
                "2, 2, WATER, 0, JUMP,  1, 5, 3, 2, 2",
                "2, 2, WATER, 0, BEAR,  2, 5, 3, 2, 1",
                "2, 2, BEAR,  4, WATER, 0, 5, 3, 2, 1",
                "2, 2, BEAR,  4, BEAR,  1, 5, 3, 2, 0"
            })
            @DisplayName("Should apply accurate energy and salmon loss rules across the grid")
            void shouldApplyGridMovementRules(int dstX, int dstY, TileType srcType, int srcOrient,
                                              TileType dstType, int dstOrient, int initEnergy, 
                                              int expEnergy, int initSalmons, int expSalmons) {
                salmonMatch.setCoordinate(new Coordinate(1, 1));
                salmonMatch.setSalmonsNumber(initSalmons);
                player.setEnergy(initEnergy);

                MatchTile src = tile(1, 1, srcType, srcOrient, 10, 1);
                MatchTile dst = tile(dstX, dstY, dstType, dstOrient, 10, 0);
                if (dstType == TileType.EAGLE) {
                    when(matchTileService.eagleToWater(dst)).thenReturn(water(dstX, dstY));
                }
                stubBatch(salmonMatch, List.of(src, dst));
                List<MoveRequest> moves = List.of(new MoveRequest(1, dstX, dstY, false));

                salmonMatchService.updateCoordinateBatch(1, moves);

                assertThat(player.getEnergy()).as("energía").isEqualTo(expEnergy);
                assertThat(salmonMatch.getSalmonsNumber()).as("salmones").isEqualTo(expSalmons);
            }

            @Test
            @DisplayName("Should jump over a full tile costing 3 energy")
            void shouldJumpOverFullTileWithCost3() {
                salmonMatch.setCoordinate(new Coordinate(1, 1));
                MatchTile src = water(1, 1);
                src.setSalmonsNumber(1);
                MatchTile full = tile(1, 2, TileType.WATER, 0, 2, 2);
                MatchTile next = water(1, 3);
                stubBatch(salmonMatch, List.of(src, full, next));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 2, false));

                salmonMatchService.updateCoordinateBatch(1, moves);

                assertThat(salmonMatch.getCoordinate()).isEqualTo(new Coordinate(1, 3));
                assertThat(player.getEnergy()).isEqualTo(2); // 5 - 3
            }

            @Test
            @DisplayName("Should throw exception when attempting to jump a full tile with insufficient energy")
            void shouldThrowWhenJumpingFullTileWithInsufficientEnergy() {
                player.setEnergy(2);
                salmonMatch.setCoordinate(new Coordinate(1, 1));
                MatchTile src = water(1, 1);
                src.setSalmonsNumber(1);
                MatchTile full = tile(1, 2, TileType.WATER, 0, 2, 2);
                MatchTile next = water(1, 3);
                stubBatch(salmonMatch, List.of(src, full, next));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 2, false));

                assertThatThrownBy(() -> salmonMatchService.updateCoordinateBatch(1, moves))
                        .isInstanceOf(NotValidMoveException.class)
                        .hasMessageContaining("The tile is full and you don't have enough energy to jump!");
            }
        }

        @Nested
        @DisplayName("Enter Spawn Logic")
        class EnterSpawn {

            @ParameterizedTest(name = "From {0}({1}) -> energy={2}->{3}, salmons={4}->{5}")
            @CsvSource({
                "WATER, 0, 5, 4, 2, 2",
                "BEAR,  3, 5, 3, 2, 1",
                "JUMP,  3, 5, 3, 2, 2"
            })
            @DisplayName("Should apply correct logic when entering spawn based on origin tile")
            void shouldEnterSpawnAsFlaggedMove(TileType myTileType, int orientation, int initEnergy, 
                                               int expEnergy, int initSalmons, int expSalmons) {
                salmonMatch.setCoordinate(new Coordinate(1, 4));
                salmonMatch.setSalmonsNumber(initSalmons);
                player.setEnergy(initEnergy);
                MatchTile myTile = tile(1, 4, myTileType, orientation, 10, 1);
                stubBatch(salmonMatch, List.of(myTile));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 5, true));

                salmonMatchService.updateCoordinateBatch(1, moves);

                assertThat(salmonMatch.getCoordinate()).isEqualTo(new Coordinate(1, 21));
                assertThat(player.getEnergy()).isEqualTo(expEnergy);
                assertThat(salmonMatch.getSalmonsNumber()).isEqualTo(expSalmons);
            }

            @Test
            @DisplayName("Should throw exception if trying to enter spawn before round 6")
            void shouldThrowWhenEnteringSpawnBeforeRound6() {
                match.setRound(5); 
                salmonMatch.setCoordinate(new Coordinate(1, 4));
                
                MatchTile myTile = water(1, 4);
                stubBatch(salmonMatch, List.of(myTile));
                
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 5, true));

                assertThatThrownBy(() -> salmonMatchService.updateCoordinateBatch(1, moves))
                        .isInstanceOf(NotValidMoveException.class)
                        .hasMessageContaining("You cannot enter spawn until round 6!");
            }

            @Test
            @DisplayName("Should not increment any destination tile's salmon count when entering spawn")
            void shouldNotIncrementDestinationTileWhenEnteringSpawn() {
                salmonMatch.setCoordinate(new Coordinate(1, 4));
                MatchTile myTile = water(1, 4);
                myTile.setSalmonsNumber(1);
                stubBatch(salmonMatch, List.of(myTile));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 5, true));

                salmonMatchService.updateCoordinateBatch(1, moves);

                assertThat(myTile.getSalmonsNumber()).isZero();
                verify(matchTileService).saveAll(List.of(myTile));
            }
        }

        @Nested
        @DisplayName("Batch Semantics & Herons")
        class BatchSemantics {

            @Test
            @DisplayName("Should skip remaining moves in the batch for a token that died earlier")
            void shouldSkipRemainingMovesForATokenThatDiedEarlierInTheSameBatch() {
                salmonMatch.setCoordinate(new Coordinate(1, 1));
                salmonMatch.setSalmonsNumber(1);
                MatchTile src = water(1, 1);
                src.setSalmonsNumber(1);
                MatchTile eagleTile = tile(1, 2, TileType.EAGLE, 0, 10, 0);
                when(matchTileService.eagleToWater(eagleTile)).thenReturn(water(1, 2));
                stubBatch(salmonMatch, List.of(src, eagleTile));

                List<MoveRequest> moves = List.of(
                        new MoveRequest(1, 1, 2, false), // kills the last salmon on this token
                        new MoveRequest(1, 1, 3, false)  // must be ignored
                );

                salmonMatchService.updateCoordinateBatch(1, moves);

                assertThat(salmonMatch.getSalmonsNumber()).isZero();
                assertThat(salmonMatch.getCoordinate()).isEqualTo(new Coordinate(1, 2)); 
                assertThat(player.getEnergy()).isEqualTo(4); 
                verify(salmonMatchRepository).deleteAll(List.of(salmonMatch));
                verify(salmonMatchRepository, never()).saveAll(List.of(salmonMatch));
            }

            @Test
            @DisplayName("Should accumulate energy costs across multiple moves in a single batch")
            void shouldAccumulateEnergyCostAcrossSeveralMovesOfTheSameBatch() {
                salmonMatch.setCoordinate(new Coordinate(1, 1));
                MatchTile src = water(1, 1);
                src.setSalmonsNumber(1);
                MatchTile mid = tile(1, 2, TileType.JUMP, 0, 10, 0); // cost 2
                MatchTile dst = water(1, 3); // cost 1
                stubBatch(salmonMatch, List.of(src, mid, dst));

                List<MoveRequest> moves = List.of(
                        new MoveRequest(1, 1, 2, false),
                        new MoveRequest(1, 1, 3, false)
                );

                salmonMatchService.updateCoordinateBatch(1, moves);

                assertThat(salmonMatch.getCoordinate()).isEqualTo(new Coordinate(1, 3));
                assertThat(player.getEnergy()).isEqualTo(2); // 5 - 2 - 1
            }

            @Test
            @DisplayName("Should apply Heron attacks only after player fully spends energy for the turn")
            void shouldApplyHeronsOnlyAfterPlayerFullySpendsEnergyForTheTurn() {
                player.setEnergy(3);

                SalmonMatch movingSalmon = salmonMatch; 
                movingSalmon.setCoordinate(new Coordinate(1, 1));

                SalmonMatch heronVictim = new SalmonMatch();
                heronVictim.setId(2);
                heronVictim.setPlayer(player);
                heronVictim.setMatch(match);
                heronVictim.setSalmonsNumber(2);
                heronVictim.setSalmon(salmon);
                heronVictim.setCoordinate(new Coordinate(0, 5));

                MatchTile src = water(1, 1);
                src.setSalmonsNumber(1);
                MatchTile full = tile(1, 2, TileType.WATER, 0, 2, 2); // forces a cost-3 jump
                MatchTile next = water(1, 3);
                MatchTile heronTile = tile(0, 5, TileType.HERON, 0, 10, 1);

                stubBatch(List.of(movingSalmon, heronVictim), List.of(src, full, next, heronTile));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 2, false));

                salmonMatchService.updateCoordinateBatch(1, moves);

                assertThat(player.getEnergy()).isEqualTo(0);
                assertThat(heronVictim.getSalmonsNumber()).isEqualTo(1); 
            }

            @Test
            @DisplayName("Should NOT apply Heron attacks when player still has energy left")
            void shouldNotApplyHeronsWhenPlayerStillHasEnergyLeft() {
                player.setEnergy(5);

                SalmonMatch movingSalmon = salmonMatch;
                movingSalmon.setCoordinate(new Coordinate(1, 1));

                SalmonMatch potentialVictim = new SalmonMatch();
                potentialVictim.setId(2);
                potentialVictim.setPlayer(player);
                potentialVictim.setMatch(match);
                potentialVictim.setSalmonsNumber(2);
                potentialVictim.setSalmon(salmon);
                potentialVictim.setCoordinate(new Coordinate(0, 5));

                MatchTile src = water(1, 1);
                src.setSalmonsNumber(1);
                MatchTile dst = water(1, 2); 
                MatchTile heronTile = tile(0, 5, TileType.HERON, 0, 10, 1);

                stubBatch(List.of(movingSalmon, potentialVictim), List.of(src, dst, heronTile));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 2, false));

                salmonMatchService.updateCoordinateBatch(1, moves);

                assertThat(player.getEnergy()).isEqualTo(4);
                assertThat(potentialVictim.getSalmonsNumber()).isEqualTo(2); // untouched
            }
        }

        @Nested
        @DisplayName("Persistence & Notifications")
        class Persistence {

            @Test
            @DisplayName("Should delete extinguished tokens, save survivors, and trigger global notifications")
            void shouldDeleteExtinguishedTokensAndSaveSurvivorsAndNotifyOnce() {
                salmonMatch.setCoordinate(new Coordinate(1, 1));
                salmonMatch.setSalmonsNumber(1);
                MatchTile src = water(1, 1);
                src.setSalmonsNumber(1);
                MatchTile eagleTile = tile(1, 2, TileType.EAGLE, 0, 10, 0);
                when(matchTileService.eagleToWater(eagleTile)).thenReturn(water(1, 2));
                stubBatch(salmonMatch, List.of(src, eagleTile));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 2, false));

                salmonMatchService.updateCoordinateBatch(1, moves);

                verify(salmonMatchRepository).deleteAll(List.of(salmonMatch));
                verify(salmonMatchRepository, never()).saveAll(List.of(salmonMatch));
                verify(playerService).save(player);
                verify(matchService).notifyPlayerAndMatchUpdate(match);
                verify(matchService).notifyTilesAndSalmons(List.of(salmonMatch), List.of(src, eagleTile));
            }

            @Test
            @DisplayName("Should save surviving tokens and avoid deletion calls when no tokens are extinguished")
            void shouldSaveSurvivingTokenAndNotifyWhenNoTokenIsExtinguished() {
                salmonMatch.setCoordinate(new Coordinate(1, 1));
                MatchTile src = water(1, 1);
                src.setSalmonsNumber(1);
                MatchTile dst = water(1, 2);
                stubBatch(salmonMatch, List.of(src, dst));
                List<MoveRequest> moves = List.of(new MoveRequest(1, 1, 2, false));

                salmonMatchService.updateCoordinateBatch(1, moves);

                verify(salmonMatchRepository).saveAll(List.of(salmonMatch));
                verify(salmonMatchRepository, never()).deleteAll(any());
                verify(matchTileService).saveAll(List.of(src, dst));
            }
        }
    }

    @Nested
    @DisplayName("Creation: create")
    class CreateTests {

        @Test
        @DisplayName("Should create 4 salmon tokens with 2 salmons each for the player")
        void shouldCreate4SalmonMatchesWithPairEach() {
            when(playerService.findById(1)).thenReturn(player);
            when(salmonService.findFirstByColor(any())).thenReturn(salmon);
            when(salmonMatchRepository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);

            salmonMatchService.create(1);

            verify(salmonMatchRepository).saveAll(any());
        }
    }

    @Nested
    @DisplayName("Sprites: setSalmonImage")
    class SetSalmonImageTests {

        @Test
        @DisplayName("Should update to single-salmon sprite when only one salmon remains")
        void shouldUpdateSalmonImageWhenOneSalmonRemains() {
            salmon.setImage("salmon_2_red");
            salmonMatch.setSalmonsNumber(1);
            Salmon singleSalmon = new Salmon();
            singleSalmon.setId(6);
            singleSalmon.setImage("salmon_1_red");
            when(salmonService.findById(6)).thenReturn(singleSalmon);

            salmonMatchService.setSalmonImage(salmonMatch);

            assertThat(salmonMatch.getSalmon().getId()).isEqualTo(6);
        }

        @Test
        @DisplayName("Should not query or update sprite if it is already a single-salmon image")
        void shouldNotUpdateSalmonImageWhenAlreadySingle() {
            salmon.setId(6);
            salmon.setImage("salmon_1_red");
            salmonMatch.setSalmonsNumber(1);

            salmonMatchService.setSalmonImage(salmonMatch);

            verify(salmonService, never()).findById(any());
        }
    }

    @Test
    void testRepositoryHelperMethods() {
        when(salmonMatchRepository.findFromGameInSpawn(1)).thenReturn(List.of(salmonMatch));
        assertThat(salmonMatchService.findFromGameInSpawn(1)).containsExactly(salmonMatch);

        SalmonMatchDTO smDto = new SalmonMatchDTO();
        when(salmonMatchRepository.findFromGameInSpawnAsDTO(1)).thenReturn(List.of(smDto));
        assertThat(salmonMatchService.findFromGameInSpawnDTO(1)).containsExactly(smDto);

        when(salmonMatchRepository.findByPlayerIdAndCoordinateIsNull(1)).thenReturn(List.of(salmonMatch));
        when(salmonMatchRepository.findByMatchIdAndCoordinateIsNull(1)).thenReturn(List.of(salmonMatch));
        assertThat(salmonMatchService.findAllFromPlayerInSea(1)).containsExactly(salmonMatch);
        assertThat(salmonMatchService.findSalmonsInSea(1)).containsExactly(salmonMatch);
        assertThat(salmonMatchService.findByMatchIdNoCoord(1)).containsExactly(salmonMatch);

        when(salmonMatchRepository.findAllFromPlayerInSpawn(1)).thenReturn(List.of(salmonMatch));
        assertThat(salmonMatchService.findSalmonsFromPlayerInSpawn(1)).containsExactly(salmonMatch);

        // findPointsFromAllSalmonInSpawn
        salmonMatch.setCoordinate(new Coordinate(1, 23));
        when(salmonMatchRepository.findById(1)).thenReturn(Optional.of(salmonMatch));
        assertThat(salmonMatchService.findPointsFromAllSalmonInSpawn(1)).isEqualTo(3);

        salmonMatch.setCoordinate(new Coordinate(1, 15));
        assertThat(salmonMatchService.findPointsFromAllSalmonInSpawn(1)).isZero();

        SMCoordinateDTO dto = new SMCoordinateDTO(null);
        when(salmonMatchRepository.findByPlayerIdInSeaAsCoordinateDTO(1)).thenReturn(List.of(dto));
        assertThat(salmonMatchService.findByPlayerIdInSeaAsCoordinateDTO(1)).containsExactly(dto);
    }

    @Test
    void testNotifySalmonMatchSpawnUpdateAndToDTOList() {
        salmonMatchService.notifySalmonMatchSpawnUpdate(List.of(salmonMatch));
        verify(messagingTemplate).convertAndSend("/topic/salmons/spawn/1", "Update salmons in spawn");

        SalmonMatchDTO dto = new SalmonMatchDTO();
        when(salmonMatchMapper.toDTOList(List.of(salmonMatch))).thenReturn(List.of(dto));
        assertThat(salmonMatchService.toDTOList(List.of(salmonMatch))).containsExactly(dto);
    }

    @Test
    void testUpdateEntityFields() {
        SalmonMatch newSm = new SalmonMatch();
        newSm.setPlayer(player);
        newSm.setSalmonsNumber(3);
        newSm.setSpawningNumber(1);
        Coordinate c = new Coordinate(2, 2);
        newSm.setCoordinate(c);
        newSm.setSalmon(salmon);
        newSm.setMatch(match);

        SalmonMatch result = salmonMatchService.updateEntityFields(newSm, salmonMatch);

        assertThat(result.getPlayer()).isEqualTo(player);
        assertThat(result.getSalmonsNumber()).isEqualTo(3);
        assertThat(result.getSpawningNumber()).isEqualTo(1);
        assertThat(result.getCoordinate()).isEqualTo(c);
        assertThat(result.getSalmon()).isEqualTo(salmon);
        assertThat(result.getMatch()).isEqualTo(match);
    }

    @Test
    void testUpdateCoordinateBatch_NullOrEmpty() {
        salmonMatchService.updateCoordinateBatch(1, null);
        salmonMatchService.updateCoordinateBatch(1, List.of());
        verifyNoInteractions(playerService);
    }
}