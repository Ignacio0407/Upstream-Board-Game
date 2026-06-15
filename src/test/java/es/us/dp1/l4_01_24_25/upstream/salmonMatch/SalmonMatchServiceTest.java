package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import es.us.dp1.l4_01_24_25.upstream.exceptions.InsufficientEnergyException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.match.Phase;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MatchTileDTO;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import es.us.dp1.l4_01_24_25.upstream.salmon.SalmonService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SalmonMatchDTO;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileType;

@ExtendWith(MockitoExtension.class)
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

        player = new Player();
        player.setId(1);
        player.setEnergy(5);
        player.setMatch(match);

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

    private void stubBasicMove(SalmonMatch sm, List<MatchTile> tiles) {
        when(salmonMatchRepository.findById(sm.getId())).thenReturn(Optional.of(sm));
        when(matchTileService.findByMatchId(match.getId())).thenReturn(tiles);
        when(matchTileService.findMyTile(any(), any())).thenAnswer(inv -> {
            Coordinate c = inv.getArgument(1);
            if (c == null) return null;
            return tiles.stream()
                    .filter(t -> t.getCoordinate() != null && t.getCoordinate().equals(c))
                    .findFirst().orElse(null);
        });
        lenient().when(salmonMatchMapper.toDTO(any())).thenReturn(new SalmonMatchDTO());
        lenient().when(matchTileService.toDTO(any())).thenReturn(new MatchTileDTO());
    }

    // ── findPointsFromAllSalmonInSpawn ────────────────────────────────────────
    // Rule: spawn points = y - 20 if y > 20, otherwise 0.

    @ParameterizedTest(name = "y={0} -> puntos={1}")
    @CsvSource({"25, 5", "21, 1", "20, 0", "15, 0"})
    void shouldCalculateSpawnPoints(int y, int expectedPoints) {
        SalmonMatch sm = new SalmonMatch();
        sm.setCoordinate(new Coordinate(1, y));
        when(salmonMatchRepository.findById(1)).thenReturn(Optional.of(sm));

        assertThat(salmonMatchService.findPointsFromAllSalmonInSpawn(1)).isEqualTo(expectedPoints);
    }

    // ── updateCoordinate — guards ─────────────────────────────────────────────

    @Test
    void shouldThrowWhenPhaseIsNotMoving() {
        match.setPhase(Phase.TILES);
        when(salmonMatchRepository.findById(1)).thenReturn(Optional.of(salmonMatch));

        assertThatThrownBy(() -> salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 0)))
                .isInstanceOf(NotValidMoveException.class)
                .hasMessageContaining("tiles have to be placed");
    }

    @Test
    void shouldThrowWhenMovingToSameCoordinate() {
        salmonMatch.setCoordinate(new Coordinate(1, 1));
        MatchTile dest = water(1, 1);
        stubBasicMove(salmonMatch, List.of(dest));

        assertThatThrownBy(() -> salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 1)))
                .isInstanceOf(NotValidMoveException.class);
    }

    @Test
    void shouldThrowWhenPlayerHasNoEnergy() {
        player.setEnergy(0);
        salmonMatch.setCoordinate(null);
        MatchTile dest = water(1, 0);
        stubBasicMove(salmonMatch, List.of(dest));

        assertThatThrownBy(() -> salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 0)))
                .isInstanceOf(InsufficientEnergyException.class);
    }

    @Test
    void shouldThrowWhenFromSeaNotMovingToY0() {
        salmonMatch.setCoordinate(null);
        MatchTile dest = water(1, 1); // y=1, not y=0
        stubBasicMove(salmonMatch, List.of(dest));

        assertThatThrownBy(() -> salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 1)))
                .isInstanceOf(NotValidMoveException.class)
                .hasMessageContaining("advance from 1 to 1");
    }

    @Test
    void shouldThrowWhenMovingMoreThan1Step() {
        salmonMatch.setCoordinate(new Coordinate(1, 1));
        MatchTile dest = water(1, 3);
        stubBasicMove(salmonMatch, List.of(water(1, 1), dest));

        assertThatThrownBy(() -> salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 3)))
                .isInstanceOf(NotValidMoveException.class);
    }

    @Test
    void shouldThrowWhenMovingBackwards() {
        salmonMatch.setCoordinate(new Coordinate(1, 2));
        MatchTile src = water(1, 2);
        MatchTile dest = water(1, 1); // y < current y
        stubBasicMove(salmonMatch, List.of(src, dest));

        assertThatThrownBy(() -> salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 1)))
                .isInstanceOf(NotValidMoveException.class);
    }

    // ── updateCoordinate — Movement from sea ────────────────────────────
    // Rule: salmones start in the sea (coordinate=null) and enter the river moving to y=0, with a cost of 1 energy.
    
    @Test
    void shouldMoveFromSeaToRiverWithCost1() {
        salmonMatch.setCoordinate(null);
        MatchTile dest = water(1, 0);
        stubBasicMove(salmonMatch, List.of(dest));

        salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 0));

        assertThat(salmonMatch.getCoordinate()).isEqualTo(new Coordinate(1, 0));
        assertThat(player.getEnergy()).isEqualTo(4);
        verify(salmonMatchRepository).save(salmonMatch);
    }

    @Test
    void shouldJumpFromSeaWhenY0TileFullAndNextNotFull() {
        salmonMatch.setCoordinate(null);
        MatchTile fullTile = tile(1, 0, TileType.WATER, 0, 2, 2); // full
        MatchTile nextTile = water(1, 1);
        stubBasicMove(salmonMatch, List.of(fullTile, nextTile));

        salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 0));

        assertThat(salmonMatch.getCoordinate()).isEqualTo(new Coordinate(1, 1));
        assertThat(player.getEnergy()).isEqualTo(2); // cost 3
    }

    @Test
    void shouldThrowWhenBothSeaTilesAreFull() {
        salmonMatch.setCoordinate(null);
        MatchTile fullTile = tile(1, 0, TileType.WATER, 0, 2, 2);
        MatchTile alsoFull = tile(1, 1, TileType.WATER, 0, 2, 2);
        stubBasicMove(salmonMatch, List.of(fullTile, alsoFull));

        assertThatThrownBy(() -> salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 0)))
                .isInstanceOf(NotValidMoveException.class)
                .hasMessageContaining("next tile is also full");
    }

    // ── updateCoordinate — eagle ─────────────────────────────────────────────
    // Rule: if a salmon enters an eagle tile while swimming, it loses a salmon and the tile converts to water.

    @Test
    void shouldLoseSalmonAndConvertEagleToWaterOnEntry() {
        salmonMatch.setCoordinate(new Coordinate(1, 0));
        MatchTile src = water(1, 0);
        MatchTile eagleTile = tile(1, 1, TileType.EAGLE, 0, 10, 0);
        when(matchTileService.eagleToWater(eagleTile)).thenReturn(water(1, 1));
        stubBasicMove(salmonMatch, List.of(src, eagleTile));

        salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 1));

        assertThat(salmonMatch.getSalmonsNumber()).isEqualTo(1);
        verify(matchTileService).eagleToWater(eagleTile);
    }

    // ── updateCoordinate — Movement in cuadricula with orientation rules ─

    @ParameterizedTest(name = "Moves to ({0},{1}) | {2}({3}) -> {4}({5}) | energy={6}->{7}, salmons={8}->{9}")
    @CsvSource({
        // ─── MOVEMENTS UP (Center to Center) ───
        // Water -> Water: cost 1, no loss
        "1, 2, WATER, 0, WATER, 0, 5, 4, 2, 2",
        // WATER -> EAGLE: loses 1 salmon, cost 1
        "1, 2, WATER, 0, EAGLE, 0, 5, 4, 2, 1",
        // JUMP (orientation 0, front): cost 2, no loss
        "1, 2, WATER, 0, JUMP,  0, 5, 3, 2, 2",
        // BEAR (orientation 0, blocks frontal entrance): cost 2, loses 1
        "1, 2, WATER, 0, BEAR,  0, 5, 3, 2, 1",
        // BEAR origen (orientation 3, blocks frontal exit): cost 2, loses 1
        "1, 2, BEAR,  3, WATER, 0, 5, 3, 2, 1",
        // BEAR (3) -> BEAR (0): cost 2, loses 2 (1 by exiting, 1 by entering)
        "1, 2, BEAR,  3, BEAR,  0, 5, 3, 2, 0",

        // ─── MOVEMENTS LEFT (Center to Left) ───
        // Water -> Water: cost 1, no loss
        "0, 2, WATER, 0, WATER, 0, 5, 4, 2, 2",
        // JUMP (orientation 4, front left): cost 2, no loss
        "0, 2, WATER, 0, JUMP,  4, 5, 3, 2, 2",
        // BEAR (orientation 5, blocks right entrance): cost 2, loses 1
        "0, 2, WATER, 0, BEAR,  5, 5, 3, 2, 1",
        // BEAR origen (orientation 2, blocks left exit): cost 2, loses 1
        "0, 2, BEAR,  2, WATER, 0, 5, 3, 2, 1",
        // BEAR (2) -> BEAR (5) diagonal: cost 2, loses 2
        "0, 2, BEAR,  2, BEAR,  5, 5, 3, 2, 0",

        // ─── MOVEMENTS RIGHT (Center to Right) ───
        // Water -> Water: cost 1, no loss
        "2, 2, WATER, 0, WATER, 0, 5, 4, 2, 2",
        // JUMP (orientation 1, front right): cost 2, no loss
        "2, 2, WATER, 0, JUMP,  1, 5, 3, 2, 2",
        // BEAR (orientation 2, blocks left entrance): cost 2, loses 1
        "2, 2, WATER, 0, BEAR,  2, 5, 3, 2, 1",
        // BEAR origen (orientation 4, blocks right exit): cost 2, loses 1
        "2, 2, BEAR,  4, WATER, 0, 5, 3, 2, 1",
        // BEAR (4) -> BEAR (1) diagonal: cost 2, loses 2
        "2, 2, BEAR,  4, BEAR,  1, 5, 3, 2, 0"
    })
    void shouldApplyGridMovementRules(int dstX, int dstY,
                                      TileType srcType, int srcOrient,
                                      TileType dstType, int dstOrient,
                                      int initEnergy, int expEnergy,
                                      int initSalmons, int expSalmons) {
        // Configuramos la posición inicial en la columna central (x=1)
        salmonMatch.setCoordinate(new Coordinate(1, 1));
        salmonMatch.setSalmonsNumber(initSalmons);
        player.setEnergy(initEnergy);

        MatchTile src = tile(1, 1, srcType, srcOrient, 10, 1);
        MatchTile dst = tile(dstX, dstY, dstType, dstOrient, 10, 0);

        if (dstType == TileType.EAGLE) {
            when(matchTileService.eagleToWater(dst)).thenReturn(water(dstX, dstY));
        }
        stubBasicMove(salmonMatch, List.of(src, dst));

        salmonMatchService.updateCoordinate(1, Map.of("x", dstX, "y", dstY));

        assertThat(player.getEnergy()).as("energía").isEqualTo(expEnergy);
        assertThat(salmonMatch.getSalmonsNumber()).as("salmones").isEqualTo(expSalmons);
    }

    // ── updateCoordinate — jump over full tile ─────────────────────────────
    // Rule: if the destination tile is full, a jump can be made by spending 3 energy.

    @Test
    void shouldJumpOverFullTileWithCost3() {
        salmonMatch.setCoordinate(new Coordinate(1, 1));
        MatchTile src = water(1, 1);
        src.setSalmonsNumber(1);
        MatchTile full = tile(1, 2, TileType.WATER, 0, 2, 2);
        MatchTile next = water(1, 3);
        stubBasicMove(salmonMatch, List.of(src, full, next));

        salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 2));

        assertThat(salmonMatch.getCoordinate()).isEqualTo(new Coordinate(1, 3));
        assertThat(player.getEnergy()).isEqualTo(2); // 5 - 3
    }

    @Test
    void shouldThrowWhenJumpingFullTileWithInsufficientEnergy() {
        player.setEnergy(2);
        salmonMatch.setCoordinate(new Coordinate(1, 1));
        MatchTile src = water(1, 1);
        src.setSalmonsNumber(1);
        MatchTile full = tile(1, 2, TileType.WATER, 0, 2, 2);
        MatchTile next = water(1, 3);
        stubBasicMove(salmonMatch, List.of(src, full, next));

        assertThatThrownBy(() -> salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 2)))
                .isInstanceOf(NotValidMoveException.class)
                .hasMessageContaining("The tile is full and you don't have enough energy to jump!");
    }

    // ── updateCoordinate — salmón eliminado al llegar a 0 ────────────────────
    // Rule: when a salmon match loses all its salmon, it is deleted.

    @Test
    void shouldDeleteSalmonMatchWhenSalmonsReachZero() {
        salmonMatch.setCoordinate(new Coordinate(1, 1));
        salmonMatch.setSalmonsNumber(1);
        MatchTile src = water(1, 1);
        src.setSalmonsNumber(1);
        MatchTile eagleTile = tile(1, 2, TileType.EAGLE, 0, 10, 0);
        when(matchTileService.eagleToWater(eagleTile)).thenReturn(water(1, 2));
        stubBasicMove(salmonMatch, List.of(src, eagleTile));

        salmonMatchService.updateCoordinate(1, Map.of("x", 1, "y", 2));

        verify(salmonMatchRepository).delete(salmonMatch);
        verify(salmonMatchRepository, never()).save(salmonMatch);
    }

    // ── enterSpawn ────────────────────────────────────────────────────────────
    // Rule: a salmon can only enter the spawning area starting from round 6.

    @Test
    void shouldThrowWhenEnteringSpawnBeforeRound6() {
        match.setRound(5);
        when(salmonMatchRepository.findById(1)).thenReturn(Optional.of(salmonMatch));

        assertThatThrownBy(() -> salmonMatchService.enterSpawn(1))
                .isInstanceOf(NotValidMoveException.class)
                .hasMessageContaining("round 6");
    }

    @Test
    void shouldEnterSpawnFromY5WithCost1() {
        match.setRound(6);
        salmonMatch.setCoordinate(new Coordinate(1, 4));
        MatchTile myTile = water(1, 4);
        myTile.setSalmonsNumber(1);

        when(salmonMatchRepository.findById(1)).thenReturn(Optional.of(salmonMatch));
        when(matchTileService.findByMatchId(match.getId())).thenReturn(List.of(myTile));
        when(matchTileService.findMyTile(any(), any())).thenReturn(myTile);
        when(salmonMatchMapper.toDTO(any())).thenReturn(new SalmonMatchDTO());
        when(matchTileService.toDTO(any())).thenReturn(new MatchTileDTO());

        salmonMatchService.enterSpawn(1);

        assertThat(salmonMatch.getCoordinate()).isEqualTo(new Coordinate(1, 21));
        assertThat(player.getEnergy()).isEqualTo(4);
    }

    // ── create ────────────────────────────────────────────────────────────────
    // Rule: each player starts with 4 salmon tokens with 2 salmon each.

    @Test
    void shouldCreate4SalmonMatchesWithPairEach() {
        when(playerService.findById(1)).thenReturn(player);
        when(salmonService.findFirstByColor(any())).thenReturn(salmon);
        when(salmonMatchRepository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);
        when(salmonMatchMapper.toDTOList(any())).thenReturn(Collections.emptyList());

        salmonMatchService.create(1);

        verify(salmonMatchRepository).saveAll(any());
    }

    // ── setSalmonImage ────────────────────────────────────────────────────────
    // Rule: When a salmon token loses a salmon (remains 1), the image is updated to the single salmon sprite.

    @Test
    void shouldUpdateSalmonImageWhenOneSalmonRemains() {
        salmon.setImage("salmon_2_red"); // imagen de pareja
        salmonMatch.setSalmonsNumber(1);
        Salmon singleSalmon = new Salmon();
        singleSalmon.setId(6);
        singleSalmon.setImage("salmon_1_red");
        when(salmonService.findById(6)).thenReturn(singleSalmon);

        salmonMatchService.setSalmonImage(salmonMatch);

        assertThat(salmonMatch.getSalmon().getId()).isEqualTo(6);
    }

    @Test
    void shouldNotUpdateSalmonImageWhenAlreadySingle() {
        salmon.setId(6);
        salmon.setImage("salmon_1_red"); // ya es individual
        salmonMatch.setSalmonsNumber(1);

        salmonMatchService.setSalmonImage(salmonMatch);

        verify(salmonService, never()).findById(any());
    }
}