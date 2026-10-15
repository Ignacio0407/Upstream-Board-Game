package upstream.player;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import upstream.coordinate.Coordinate;
import upstream.player.playerdto.PlayerDTO;
import upstream.exceptions.InsufficientEnergyException;
import upstream.exceptions.ResourceNotFoundException;
import upstream.match.MatchService;
import upstream.player.PlayerService.NextPlayerResult;
import upstream.salmonmatch.SalmonMatchService;
import upstream.salmonmatch.dto.SMCoordinateDTO;
import upstream.user.User;
import upstream.user.UserService;

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

    // findMyPlayer
    @Test
    @DisplayName("findMyPlayer - Happy Path: Devuelve el jugador del usuario autenticado")
    void shouldFindMyPlayerSuccessfully() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testUser");

        User mockUser = new User();
        mockUser.setId(1);
        when(userService.findUserByName("testUser")).thenReturn(mockUser);

        Player mockPlayer = new Player();
        mockPlayer.setId(10);
        when(playerRepository.findByUserPlayer_IdAndMatch_Id(1, 99)).thenReturn(mockPlayer); 

        Player result = playerService.findMyPlayer(99);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10);
        verify(userService).findUserByName("testUser");
        
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("findMyPlayer - Negative Path: Lanza excepción si el usuario no existe")
    void shouldThrowExceptionWhenUserNotFound() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("unknownUser");

        when(userService.findUserByName("unknownUser")).thenThrow(new ResourceNotFoundException("User not found"));

        assertThatThrownBy(() -> playerService.findMyPlayer(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        SecurityContextHolder.clearContext();
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
        assertThatNoException().isThrownBy(() -> 
            playerService.throwExceptionNotEnoughEnergy(player, 5)
        );
    }

    @Test
    void shouldNotThrowWhenEnergyIsExact() {
        player.setEnergy(3);
        assertThatNoException().isThrownBy(() -> 
            playerService.throwExceptionNotEnoughEnergy(player, 3)
        );
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
        assertThat(player.getEnergy()).isZero();
        assertThat(player.getPlayerOrder()).isEqualTo(10);
        verify(playerRepository).save(player);
    }

    // ── setPlayerNoEnergy ─────────────────────────────────────────────────────
    // Rule: When a player has finished (all in spawning area) they cannot spend more movement points — their energy is set to 0.

    @Test
    void shouldSetPlayerEnergyToZeroWhenFinished() {
        player.setEnergy(3);

        playerService.setPlayerNoEnergy(player);

        assertThat(player.getEnergy()).isZero();
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
        assertThat(player.getEnergy()).isZero();
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

        assertThat(player.getEnergy()).isZero();
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

    @Test
    void testFindPlayersByMatchAndDTO() {
        when(playerRepository.findByMatchId(10)).thenReturn(List.of(player));
        assertThat(playerService.findPlayersByMatch(10)).containsExactly(player);

        PlayerDTO dto = new PlayerDTO();
        when(playerRepository.findPlayersByMatchAsDTO(10)).thenReturn(List.of(dto));
        assertThat(playerService.findPlayersByMatchAsDTO(10)).containsExactly(dto);
    }

    @Test
    void testFindAlivePlayersByMatchSortedPlayerOrder() {
        when(playerRepository.findByMatchIdAndAliveTrueOrderByPlayerOrderAsc(10)).thenReturn(List.of(player));
        assertThat(playerService.findAlivePlayersByMatchSortedPlayerOrder(10)).containsExactly(player);
    }

    @Test
    void testUpdateEntityFields() {
        Player playerToUpdate = new Player();
        Player newPlayer = new Player();
        newPlayer.setName("NewName");
        newPlayer.setColor(Color.RED);
        newPlayer.setAlive(true);
        newPlayer.setEnergy(4);
        newPlayer.setPoints(10);
        newPlayer.setPlayerOrder(2);

        Player result = playerService.updateEntityFields(playerToUpdate, newPlayer);

        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.getColor()).isEqualTo(Color.RED);
        assertThat(result.getAlive()).isTrue();
        assertThat(result.getEnergy()).isEqualTo(4);
        assertThat(result.getPoints()).isEqualTo(10);
        assertThat(result.getPlayerOrder()).isEqualTo(2);
    }

    @Test
    void testIsUserInMatch() {
        when(playerRepository.existsByUsernameAndMatchId("user1", 5)).thenReturn(true);
        assertThat(playerService.isUserInMatch("user1", 5)).isTrue();
    }

    @Test
    void testNotifyPlayersUpdate() {
        PlayerDTO dto = new PlayerDTO();
        when(playerRepository.findByMatchIdOrderByPlayerOrderAscAsDTO(5)).thenReturn(List.of(dto));

        playerService.notifyPlayersUpdate(5);
        verify(messagingTemplate).convertAndSend("/topic/players/5", List.of(dto));
    }

    @Test
    void testDeleteById() {
        upstream.match.Match match = new upstream.match.Match();
        match.setPlayersNumber(3);
        player.setMatch(match);

        when(playerRepository.findById(1)).thenReturn(Optional.of(player));

        playerService.deleteById(1);

        assertThat(match.getPlayersNumber()).isEqualTo(2);
        verify(matchService).save(match);
        verify(matchService).notifyMatchUpdate(match);
        verify(playerRepository).deleteById(1);
    }

    @Test
    void testSetEnergyToZero_SuccessAndAccessDenied() {
        upstream.match.Match match = new upstream.match.Match();
        match.setActualPlayer(player);
        player.setMatch(match);

        when(playerRepository.findById(1)).thenReturn(Optional.of(player));

        // Same actual player -> succeeds
        playerService.setEnergyToZero(1);
        assertThat(player.getEnergy()).isZero();

        // Different actual player -> throws AccessDeniedException
        Player otherPlayer = new Player();
        otherPlayer.setId(2);
        match.setActualPlayer(otherPlayer);

        assertThatThrownBy(() -> playerService.setEnergyToZero(1))
                .isInstanceOf(upstream.exceptions.AccessDeniedException.class);
    }

    @Test
    void testRegenerateEnergy() {
        player.setEnergy(1);
        when(playerRepository.findById(1)).thenReturn(Optional.of(player));
        PlayerDTO dto = new PlayerDTO();
        when(playerMapper.toDTO(player)).thenReturn(dto);

        PlayerDTO result = playerService.regenerateEnergy(1);

        assertThat(player.getEnergy()).isEqualTo(5);
        assertThat(result).isEqualTo(dto);
        verify(playerRepository).save(player);
    }

    @Test
    void testPlayersForChangePhase() {
        when(playerRepository.findByMatchIdAndAliveTrue(10)).thenReturn(List.of(player));
        when(salmonMatchService.findByPlayerIdInRiverAsCoordinateDTO(1)).thenReturn(List.of(new SMCoordinateDTO(null)));

        List<Player> result = playerService.playersForChangePhase(10);
        assertThat(result).containsExactly(player);
    }

}