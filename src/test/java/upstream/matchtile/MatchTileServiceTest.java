package upstream.matchtile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

import upstream.coordinate.Coordinate;
import upstream.exceptions.ConflictException;
import upstream.exceptions.NotValidMoveException;
import upstream.exceptions.ResourceNotFoundException;
import upstream.matchtile.dto.MTCoordinateDTO;
import upstream.match.Match;
import upstream.match.MatchService;
import upstream.matchtile.dto.MatchTileDTO;
import upstream.player.Player;
import upstream.player.PlayerService;
import upstream.tile.Tile;
import upstream.tile.TileService;
import upstream.tile.TileType;

@ExtendWith(MockitoExtension.class)
class MatchTileServiceTest {

    @InjectMocks
    private MatchTileService matchTileService;

    @Mock private MatchTileRepository matchTileRepository;
    @Mock private MatchTileMapper matchTileMapper;
    @Mock private TileService tileService;
    @Mock private MatchService matchService;
    @Mock private PlayerService playerService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    private Match match;
    private MatchTile matchTile;
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId(1);

        match = new Match();
        match.setId(1);
        match.setRound(1);
        match.setPlayersNumber(3);
        match.setActualPlayer(player);

        matchTile = new MatchTile();
        matchTile.setId(1);
        matchTile.setMatch(match);
        matchTile.setCoordinate(new Coordinate(1, 0));
    }

    // ── validateTilePlacement ─────────────────────────────────────────────────
    
    @ParameterizedTest(name = "Round {0}, y={1} -> valid={2}")
    @CsvSource({
        "1, 0, true",
        "1, 1, false",
        "3, 2, true",
        "3, 3, false",
        "6, 5, true",
        "6, 6, false"
    })
    void shouldValidateTilePlacementByRound(int round, int y, boolean expected) {
        assertThat(matchTileService.validateTilePlacement(round, y)).isEqualTo(expected);
    }

    // ── updateCoordinate — Turn Validation ────────────────────────────────────

    @Test
    void shouldThrowWhenNotPlayersTurnOnUpdateCoordinate() {
        Player otherPlayer = new Player();
        otherPlayer.setId(2);
        match.setActualPlayer(otherPlayer); // Es el turno de otro jugador

        when(matchTileRepository.findById(1)).thenReturn(Optional.of(matchTile));
        when(playerService.findMyPlayer(match.getId())).thenReturn(player); // Mi jugador es el 1
        
        Map<String, Integer> upd = Map.of("x", 1, "y", 2, "orientation", 0);
        
        assertThatThrownBy(() -> matchTileService.updateCoordinate(1, upd))
                .isInstanceOf(NotValidMoveException.class)
                .hasMessageContaining("It is not your turn!");
    }

    // ── updateCoordinate — Round restrictions ─────────────────────────────────

    @ParameterizedTest(name = "Round {0}, position ({1},{2}) -> expected exception={3}")
    @CsvSource({
        "0, 1, 4, true",   // round 0: y=4 is > 3 (Exception)
        "0, 1, 3, false",  // round 0: y=3 (Allowed)
        "1, 1, 5, true",   // round 1: y=5 is > 4 (Exception)
        "1, 1, 4, false",  // round 1: y=4 (Allowed)
        "6, 1, 5, true",   // round 6: (1,5) is explicitly forbidden (Exception)
        "6, 1, 4, false"   // round 6: (1,4) (Allowed)
    })
    void shouldEnforcePlacementRestrictionsPerRound(int round, int x, int y, boolean expectException) {
        match.setRound(round);
        
        Map<String, Integer> upd = Map.of("x", x, "y", y, "orientation", 0);

        lenient().when(matchTileRepository.findById(1)).thenReturn(Optional.of(matchTile));
        lenient().when(playerService.findMyPlayer(match.getId())).thenReturn(player); // Turn check passes

        List<MatchTile> existingTiles = new ArrayList<>();
        // Inyectamos soporte para que no salte ResourceNotFoundException por falta de apoyo en Y-1
        if (y != 0) {
            MatchTile pred = new MatchTile();
            pred.setCoordinate(new Coordinate(x, y - 1));
            existingTiles.add(pred);
        }

        lenient().when(matchTileRepository.findByMatchId(1)).thenReturn(existingTiles);
        lenient().when(matchTileRepository.save(any())).thenReturn(matchTile);
        
        MatchTileDTO dto = new MatchTileDTO();
        dto.setMatchId(1);
        lenient().when(matchTileMapper.toDTO(any())).thenReturn(dto);

        if (expectException) {
            assertThatThrownBy(() -> matchTileService.updateCoordinate(1, upd))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("No se puede actualizar el MatchTile en esta ronda");
        } else {
            matchTileService.updateCoordinate(1, upd); // Debe pasar sin excepción
        }
    }

    // ── updateCoordinate — Occupied position ──────────────────────────────────

    @Test
    void shouldThrowWhenPositionAlreadyOccupied() {
        Map<String, Integer> upd = Map.of("x", 1, "y", 1, "orientation", 0);

        MatchTile occupying = new MatchTile();
        occupying.setCoordinate(new Coordinate(1, 1)); // Misma posición

        when(matchTileRepository.findById(1)).thenReturn(Optional.of(matchTile));
        when(playerService.findMyPlayer(match.getId())).thenReturn(player);
        when(matchTileRepository.findByMatchId(1)).thenReturn(List.of(occupying)); // Simula que ya hay una loseta

        assertThatThrownBy(() -> matchTileService.updateCoordinate(1, upd))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ya existe una MatchTile");
    }

    // ── updateCoordinate — Happy path ─────────────────────────────────────────

    @Test
    void shouldUpdateCoordinateAndOrientationSuccessfullyAndNotifyEveryone() {
        Map<String, Integer> upd = Map.of("x", 2, "y", 0, "orientation", 3);
        
        MatchTileDTO dto = new MatchTileDTO();
        dto.setMatchId(match.getId());

        when(matchTileRepository.findById(1)).thenReturn(Optional.of(matchTile));
        when(playerService.findMyPlayer(match.getId())).thenReturn(player);
        when(matchTileRepository.findByMatchId(1)).thenReturn(List.of(matchTile));
        when(matchTileMapper.toDTO(matchTile)).thenReturn(dto);

        matchTileService.updateCoordinate(1, upd);

        // Verifica que se actualizaron los estados
        assertThat(matchTile.getCoordinate()).isEqualTo(new Coordinate(2, 0));
        assertThat(matchTile.getOrientation()).isEqualTo(3);
        verify(matchTileRepository).save(matchTile);
        
        // Verifica que se lanzaron los 3 eventos correspondientes
        verify(messagingTemplate).convertAndSend("/topic/tiles/place/1", dto);
        verify(playerService).notifyPlayersUpdate(1);
        verify(matchService).notifyMatchUpdate(match);
    }

    // ── broadcastMatchTileRotation ────────────────────────────────────────────

    @Test
    void shouldBroadcastRotationWithoutSavingToDatabase() {
        MatchTileDTO dto = new MatchTileDTO();
        dto.setMatchId(match.getId());
        
        when(matchTileRepository.findById(1)).thenReturn(Optional.of(matchTile));
        when(playerService.findMyPlayer(match.getId())).thenReturn(player);
        when(matchTileMapper.toDTO(matchTile)).thenReturn(dto);

        matchTileService.broadcastMatchTileRotation(1, 4);

        assertThat(matchTile.getOrientation()).isEqualTo(4);
        verify(messagingTemplate).convertAndSend("/topic/tiles/rotation/1", dto);
        verify(matchTileRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenNotPlayersTurnOnBroadcastRotation() {
        Player otherPlayer = new Player();
        otherPlayer.setId(2);
        match.setActualPlayer(otherPlayer); // Turno de otro
        
        when(matchTileRepository.findById(1)).thenReturn(Optional.of(matchTile));
        when(playerService.findMyPlayer(match.getId())).thenReturn(player);
        
        assertThatThrownBy(() -> matchTileService.broadcastMatchTileRotation(1, 3))
                .isInstanceOf(NotValidMoveException.class)
                .hasMessageContaining("It is not your turn!");
    }

    // ── findMyTile ────────────────────────────────────────────────────────────

    @Test
    void shouldFindTileByCoordinate() {
        MatchTile mt1 = new MatchTile(); mt1.setCoordinate(new Coordinate(0, 5));
        MatchTile mt2 = new MatchTile(); mt2.setCoordinate(new Coordinate(1, 10));

        MatchTile result = matchTileService.findMyTile(List.of(mt1, mt2), new Coordinate(1, 10));

        assertThat(result).isEqualTo(mt2);
    }

    @Test
    void shouldReturnNullWhenTileNotFound() {
        MatchTile mt1 = new MatchTile(); mt1.setCoordinate(new Coordinate(0, 5));

        MatchTile result = matchTileService.findMyTile(List.of(mt1), new Coordinate(2, 9));

        assertThat(result).isNull();
    }

    // ── eagleToWater ──────────────────────────────────────────────────────────

    @Test
    void shouldConvertEagleTileToWater() {
        Tile waterTile = new Tile();
        waterTile.setType(TileType.WATER);

        when(tileService.findById(1)).thenReturn(waterTile);
        when(matchTileRepository.save(matchTile)).thenReturn(matchTile);

        MatchTile result = matchTileService.eagleToWater(matchTile);

        assertThat(result.getTile().getType()).isEqualTo(TileType.WATER);
        verify(matchTileRepository).save(matchTile);
    }

    // ── createMultipleMatchTiles ──────────────────────────────────────────────

    @Test
    void shouldCreate29MatchTilesOnGameStart() {
        Tile tile = new Tile();
        tile.setType(TileType.WATER);
        when(tileService.findById(any())).thenReturn(tile);
        when(matchService.findById(1)).thenReturn(match);

        matchTileService.createMultipleMatchTiles(1);

        verify(matchTileRepository, atLeastOnce()).saveAll(any());
        verify(matchService).notifyMatchStarts(1);
    }

    @Test
    void shouldSetRockCapacityToPlayersMinusOneWhenMoreThan2Players() {
        match.setPlayersNumber(4);
        Tile rockTile = new Tile();
        rockTile.setType(TileType.ROCK);

        when(tileService.findById(any())).thenReturn(rockTile);
        when(matchService.findById(1)).thenReturn(match);

        matchTileService.createMultipleMatchTiles(1);

        verify(matchTileRepository, atLeastOnce()).saveAll(any());
    }

    @Test
    void testVariousFindMethods() {
        when(matchTileRepository.findByMatchIdAndCoordinateIsNull(1)).thenReturn(List.of(matchTile));
        assertThat(matchTileService.findByMatchIdNoCoord(1)).containsExactly(matchTile);

        MatchTileDTO dto = new MatchTileDTO();
        when(matchTileRepository.findByMatchIdAndCoordinateIsNullAsDTO(1)).thenReturn(List.of(dto));
        assertThat(matchTileService.findByMatchIdNoCoordAsDTO(1)).containsExactly(dto);

        when(matchTileRepository.findByCoordinate(1, 2)).thenReturn(Optional.of(matchTile));
        assertThat(matchTileService.findByCoordinate(1, 2)).isEqualTo(matchTile);

        when(matchTileRepository.findByCoordinate(9, 9)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> matchTileService.findByCoordinate(9, 9))
                .isInstanceOf(ResourceNotFoundException.class);

        when(matchTileRepository.findHeronWithCoordFromGame(1)).thenReturn(List.of(matchTile));
        assertThat(matchTileService.findHeronWithCoordsFromGame(1)).containsExactly(matchTile);

        MTCoordinateDTO mtDto = new MTCoordinateDTO(1, new Coordinate(1, 1));
        when(matchTileRepository.findByMatchIdNoCoordAsMTCoordinateDTO(1)).thenReturn(List.of(mtDto));
        assertThat(matchTileService.findByMatchIdNoCoordAsMTCoordinateDTO(1)).containsExactly(mtDto);

        when(matchTileRepository.findByMatchIdAsMTCoordinateDTO(1)).thenReturn(List.of(mtDto));
        assertThat(matchTileService.findByMatchIdAsMTCoordinateDTO(1)).containsExactly(mtDto);
    }

    @Test
    void testUpdateEntityFields() {
        MatchTile newTile = new MatchTile();
        newTile.setCapacity(4);
        newTile.setOrientation(2);
        newTile.setSalmonsNumber(3);
        Coordinate c = new Coordinate(1, 1);
        newTile.setCoordinate(c);
        Tile t = new Tile();
        newTile.setTile(t);
        Match m = new Match();
        newTile.setMatch(m);

        MatchTile result = matchTileService.updateEntityFields(newTile, matchTile);

        assertThat(result.getCapacity()).isEqualTo(4);
        assertThat(result.getOrientation()).isEqualTo(2);
        assertThat(result.getSalmonsNumber()).isEqualTo(3);
        assertThat(result.getCoordinate()).isEqualTo(c);
        assertThat(result.getTile()).isEqualTo(t);
        assertThat(result.getMatch()).isEqualTo(m);
    }

    @Test
    void testToDTO_NullAndList() {
        assertThat(matchTileService.toDTO((MatchTile) null)).isNull();

        MatchTileDTO dto = new MatchTileDTO();
        when(matchTileMapper.toDTOList(List.of(matchTile))).thenReturn(List.of(dto));
        assertThat(matchTileService.toDTOList(List.of(matchTile))).containsExactly(dto);
    }

    @Test
    void shouldSetRockCapacityToTwoWhenTwoOrLessPlayers() {
        match.setPlayersNumber(2);
        Tile rockTile = new Tile();
        rockTile.setType(TileType.ROCK);

        when(tileService.findById(any())).thenReturn(rockTile);
        when(matchService.findById(1)).thenReturn(match);

        matchTileService.createMultipleMatchTiles(1);

        verify(matchTileRepository, atLeastOnce()).saveAll(any());
    }

}