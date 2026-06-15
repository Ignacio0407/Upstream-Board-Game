package es.us.dp1.l4_01_24_25.upstream.player;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService.NextPlayerResult;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SMCoordinateDTO;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @InjectMocks
    private PlayerService playerService;

    @Mock private PlayerRepository playerRepository;
    @Mock private PlayerMapper playerMapper;
    @Mock private SalmonMatchService salmonMatchService;
    @Mock private UserService userService;
    @Mock private MatchService matchService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    private List<Player> players;
    private Player player;

    @BeforeEach
    void setUp() {
        players = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Player p = new Player();
            p.setId(i + 1);
            p.setPlayerOrder(i);
            players.add(p);
        }
        player = players.get(0);
        player.setEnergy(5);
        player.setAlive(true);
        player.setPoints(0);
    }

    // ── findNextPlayer ────────────────────────────────────────────────────────
    // Rule: Turns are in clockwise and initial player rotates each round — findNextPlayer implements that rotation.

    @ParameterizedTest(name = "Player in index {0} -> next in index {1}")
    @CsvSource({"0,1", "1,2", "2,3", "3,0"})
    void shouldFindNextPlayerInRotation(int currentIndex, int expectedNextIndex) {
        NextPlayerResult result = playerService.findNextPlayer(
                players.get(currentIndex), players);

        assertThat(result.player().getId()).isEqualTo(players.get(expectedNextIndex).getId());
        assertThat(result.nextIndex()).isEqualTo(expectedNextIndex);
    }

    // ── throwExceptionNotEnoughEnergy ─────────────────────────────────────────
    // Rule: Each player has 5 energy points at the start of their turn to move their salmon. Each movement costs 1 point.
    // If there is not enough energy to perform a movement, an InsufficientEnergyException is thrown.

    @Test
    void shouldNotThrowWhenEnergyIsSufficient() {
        player.setEnergy(5);
        playerService.throwExceptionNotEnoughEnergy(player, 5);
    }

    @Test
    void shouldNotThrowWhenEnergyIsExact() {
        player.setEnergy(3);
        playerService.throwExceptionNotEnoughEnergy(player, 3);
    }

    @Test
    void shouldThrowWhenEnergyIsInsufficient() {
        player.setEnergy(2);
        assertThatThrownBy(() -> playerService.throwExceptionNotEnoughEnergy(player, 3))
                .isInstanceOf(InsufficientEnergyException.class)
                .hasMessageContaining("Insufficient energy");
    }

    @Test
    void shouldThrowWhenEnergyIsZeroAndMovementRequired() {
        player.setEnergy(0);
        assertThatThrownBy(() -> playerService.throwExceptionNotEnoughEnergy(player, 1))
                .isInstanceOf(InsufficientEnergyException.class);
    }

    // ── checkPlayerFinished ───────────────────────────────────────────────────
    // Rule: Player is finished when all their salmon have reached the spawning area (coordinate.y > 20).

    @Test
    void shouldReturnTrueWhenAllSalmonsInSpawn() {
        when(salmonMatchService.findByPlayerIdAsCoordinateDTO(1)).thenReturn(List.of(
                new SMCoordinateDTO(new Coordinate(1, 21)),
                new SMCoordinateDTO(new Coordinate(2, 23))
        ));

        assertThat(playerService.checkPlayerFinished(1)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSomeSalmonsStillInRiver() {
        when(salmonMatchService.findByPlayerIdAsCoordinateDTO(1)).thenReturn(List.of(
                new SMCoordinateDTO(new Coordinate(1, 21)),
                new SMCoordinateDTO(new Coordinate(1, 10))  // aún en el río
        ));

        assertThat(playerService.checkPlayerFinished(1)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNoSalmons() {
        when(salmonMatchService.findByPlayerIdAsCoordinateDTO(1))
                .thenReturn(Collections.emptyList());

        assertThat(playerService.checkPlayerFinished(1)).isFalse();
    }

    // ── checkPlayerIsDead ─────────────────────────────────────────────────────
    // Rule: Player is dead when they have no salmon remaining (all have been eaten by eagles, bears, or herons).

    @Test
    void shouldReturnTrueWhenNoSalmonsRemain() {
        when(salmonMatchService.findByPlayerIdAsCoordinateDTO(1))
                .thenReturn(Collections.emptyList());

        assertThat(playerService.checkPlayerIsDead(1)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSalmonsRemain() {
        when(salmonMatchService.findByPlayerIdAsCoordinateDTO(1)).thenReturn(List.of(
                new SMCoordinateDTO(new Coordinate(1, 10))
        ));

        assertThat(playerService.checkPlayerIsDead(1)).isFalse();
    }

    // ── checkPlayerNoEnergy ───────────────────────────────────────────────────

    @Test
    void shouldReturnTrueWhenEnergyIsZero() {
        player.setEnergy(0);
        assertThat(playerService.checkPlayerNoEnergy(player)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEnergyIsPositive() {
        player.setEnergy(3);
        assertThat(playerService.checkPlayerNoEnergy(player)).isFalse();
    }

    // ── setPlayerDead ─────────────────────────────────────────────────────────
    // Rule: When a player dies, they lose all their energy and their token is removed from the game.

    @Test
    void shouldSetPlayerDeadCorrectly() {
        player.setAlive(true);
        player.setEnergy(3);

        playerService.setPlayerDead(player);

        assertThat(player.getAlive()).isFalse();
        assertThat(player.getEnergy()).isEqualTo(0);
        assertThat(player.getPlayerOrder()).isEqualTo(10);
        verify(playerRepository).save(player);
    }

    // ── setPlayerNoEnergy ─────────────────────────────────────────────────────
    // Rule: When a player has finished (all in spawning area) they cannot spend more movement points — their energy is set to 0.

    @Test
    void shouldSetPlayerEnergyToZeroWhenFinished() {
        player.setEnergy(3);

        playerService.setPlayerNoEnergy(player);

        assertThat(player.getEnergy()).isEqualTo(0);
        verify(playerRepository).save(player);
    }

    // ── checkPlayersDeadOrFinished ────────────────────────────────────────────
    // Rule: At each turn end it is checked if any player has died or finished to update their state.

    @Test
    void shouldMarkDeadPlayerWhenNoSalmonsRemain() {
        player.setAlive(true);
        player.setEnergy(5);

        when(salmonMatchService.findByPlayerIdAsCoordinateDTO(1))
                .thenReturn(Collections.emptyList());

        playerService.checkPlayersDeadOrFinished(List.of(player));

        assertThat(player.getAlive()).isFalse();
        assertThat(player.getEnergy()).isEqualTo(0);
    }

    @Test
    void shouldMarkNoEnergyWhenAllSalmonsInSpawn() {
        player.setAlive(true);
        player.setEnergy(5);

        when(salmonMatchService.findByPlayerIdAsCoordinateDTO(1)).thenReturn(List.of(
                new SMCoordinateDTO(new Coordinate(1, 22)),
                new SMCoordinateDTO(new Coordinate(2, 24))
        ));

        playerService.checkPlayersDeadOrFinished(List.of(player));

        assertThat(player.getEnergy()).isEqualTo(0);
        assertThat(player.getAlive()).isTrue();
    }

    @Test
    void shouldNotModifyPlayerStillInRiver() {
        player.setAlive(true);
        player.setEnergy(5);

        when(salmonMatchService.findByPlayerIdAsCoordinateDTO(1)).thenReturn(List.of(
                new SMCoordinateDTO(new Coordinate(1, 10))
        ));

        playerService.checkPlayersDeadOrFinished(List.of(player));

        assertThat(player.getAlive()).isTrue();
        assertThat(player.getEnergy()).isEqualTo(5);
    }
}