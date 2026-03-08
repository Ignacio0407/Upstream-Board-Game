package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.BadRequestException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.LobbyPlayerDTO;
import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @InjectMocks
    private PlayerService playerService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private UserService userService;

    @Mock
    private MatchService matchService;

    @Mock
    private SalmonMatchService salmonMatchService;

    @Mock
    private PlayerMapper playerMapper;

    private static final Integer MATCH_ID = 1;
    private static final Integer PLAYER_ID = 1;

    // Player entity de prueba
    private Player createSamplePlayer(Integer id, Integer energy, Integer points) {
        Player player = new Player();
        player.setId(id);
        player.setName("Test Player");
        player.setColor(Color.RED);
        player.setEnergy(energy);
        player.setPoints(points);
        player.setAlive(true);
        return player;
    }

    /* *********************************************************************
     * Tests parametrizados para el método save (BaseService)
     ********************************************************************* */
    @ParameterizedTest
    @MethodSource("saveScenarios")
    void testSavePlayer_Parametrized(Player inputPlayer, 
                                    boolean shouldThrowException, 
                                    Player expectedOutput) {
        if (shouldThrowException) {
            doThrow(new DataAccessException("DB Error") {})
                .when(playerRepository).save(inputPlayer);
        } else {
            when(playerRepository.save(inputPlayer)).thenReturn(expectedOutput);
        }

        if (shouldThrowException) {
            assertThrows(DataAccessException.class, () -> playerService.save(inputPlayer));
        } else {
            Player result = playerService.save(inputPlayer);
            assertNotNull(result);
            assertEquals(expectedOutput.getId(), result.getId());
            verify(playerRepository, times(1)).save(inputPlayer);
        }
    }

    static Stream<Arguments> saveScenarios() {
        Player validPlayer = new Player();
        validPlayer.setId(1);
        validPlayer.setName("Valid Player");
        
        Player playerWithNullFields = new Player();
        playerWithNullFields.setName(null);
        
        Player playerWithInvalidEnergy = new Player();
        playerWithInvalidEnergy.setId(2);
        playerWithInvalidEnergy.setName("Invalid Energy");
        playerWithInvalidEnergy.setEnergy(-1); // Valor inválido

        return Stream.of(
            // Caso 1: Guardado exitoso
            Arguments.of(validPlayer, false, validPlayer),
            
            // Caso 2: Excepción al guardar
            Arguments.of(validPlayer, true, null),
            
            // Caso 3: Jugador con campos nulos
            Arguments.of(playerWithNullFields, true, null),
            
            // Caso 4: Jugador con energía inválida
            Arguments.of(playerWithInvalidEnergy, true, null)
        );
    }

    /* *********************************************************************
     * Tests parametrizados para findPlayersByMatch
     ********************************************************************* */
    @ParameterizedTest
    @MethodSource("findPlayersScenarios")
    void testFindPlayersByMatch_Parametrized(Integer matchId, 
                                          List<Player> dbResponse,
                                          boolean shouldThrowException) {
        if (shouldThrowException) {
            doThrow(new DataAccessException("DB Error") {})
                .when(playerRepository).findByMatchId(matchId);
        } else {
            when(playerRepository.findByMatchId(matchId)).thenReturn(dbResponse);
        }

        if (shouldThrowException) {
            assertThrows(DataAccessException.class, () -> 
                playerService.findPlayersByMatch(matchId));
        } else {
            List<Player> result = playerService.findPlayersByMatch(matchId);
            assertNotNull(result);
            assertEquals(dbResponse.size(), result.size());
        }
    }

    static Stream<Arguments> findPlayersScenarios() {
        Player player1 = new Player(); player1.setId(1);
        Player player2 = new Player(); player2.setId(2);
        
        return Stream.of(
            // Caso 1: Partida con jugadores
            Arguments.of(MATCH_ID, Arrays.asList(player1, player2), false),
            
            // Caso 2: Partida sin jugadores
            Arguments.of(MATCH_ID, Collections.emptyList(), false),
            
            // Caso 3: Partida no existente
            Arguments.of(999, Collections.emptyList(), false),
            
            // Caso 4: Error de base de datos
            Arguments.of(MATCH_ID, null, true)
        );
    }

    /* *********************************************************************
     * Tests parametrizados para updateEnergy
     ********************************************************************* */
    @ParameterizedTest
    @MethodSource("updateEnergyScenarios")
    void testUpdateEnergy_Parametrized(Integer playerId, 
                                   Integer energyUsed,
                                   Integer initialEnergy,
                                   boolean shouldThrowException) {
        Player player = createSamplePlayer(playerId, initialEnergy, 0);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        
        if (shouldThrowException && energyUsed > initialEnergy) {
            assertThrows(BadRequestException.class, () -> 
                playerService.updateEnergy(playerId, energyUsed));
        } else if (shouldThrowException) {
            when(playerRepository.findById(playerId)).thenThrow(new DataAccessException("DB Error") {});
            assertThrows(DataAccessException.class, () -> 
                playerService.updateEnergy(playerId, energyUsed));
        } else {
            PlayerDTO updatedPlayer = playerService.updateEnergy(playerId, energyUsed);
            assertNotNull(updatedPlayer);
            assertEquals(initialEnergy - energyUsed, updatedPlayer.getEnergy());
        }
    }

    static Stream<Arguments> updateEnergyScenarios() {
        return Stream.of(
            // Caso 1: Uso exitoso de energía
            Arguments.of(PLAYER_ID, 2, 5, false),
            
            // Caso 2: Uso total de energía
            Arguments.of(PLAYER_ID, 5, 5, false),
            
            // Caso 3: Energía insuficiente
            Arguments.of(PLAYER_ID, 6, 5, true),
            
            // Caso 4: Error al buscar jugador
            Arguments.of(PLAYER_ID, 2, 5, true)
        );
    }

    /* *********************************************************************
     * Tests parametrizados para createPlayerInMatch
     ********************************************************************* */
    @ParameterizedTest
@MethodSource("createPlayerScenarios")
void testCreatePlayerInMatch_Parametrized(Map<String, String> playerDTO,
                                          boolean userExists,
                                          boolean matchExists) {
    Match match = new Match();
    match.setId(MATCH_ID);
    match.setPlayersNumber(0);

    Integer userId = null;
    try {
        userId = Integer.valueOf(playerDTO.get("userId"));
    } catch (NumberFormatException | NullPointerException e) {
        // Valor inválido o nulo
    }

    if (userExists && matchExists && userId != null) {
        User user = new User();
        user.setId(userId);
        when(userService.findById(userId)).thenReturn(user);
        when(matchService.findById(MATCH_ID)).thenReturn(match);
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player p = invocation.getArgument(0);
            p.setId(1);
            return p;
        });
    } else if (userExists) {
        when(userService.findById(userId)).thenThrow(new ResourceNotFoundException("User not found"));
    } else if (matchExists) {
        when(matchService.findById(MATCH_ID)).thenThrow(new ResourceNotFoundException("Match not found"));
    }

    if (userExists && matchExists && userId != null) {
        LobbyPlayerDTO result = playerService.createPlayerInMatch(MATCH_ID, playerDTO);
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(1, match.getPlayersNumber());
    } else {
        assertThrows(ResourceNotFoundException.class, () ->
            playerService.createPlayerInMatch(MATCH_ID, playerDTO));
    }
}

    static Stream<Arguments> createPlayerScenarios() {
        Map<String, String> validDto = new HashMap<>();
        validDto.put("color", "GREEN");
        validDto.put("userId", "1");

        Map<String, String> invalidDto = new HashMap<>();
        // Dejar claves sin valores para simular entrada inválida

        return Stream.of(
            Arguments.of(validDto, true, true),
            Arguments.of(validDto, false, true),
            Arguments.of(validDto, true, false),
            Arguments.of(invalidDto, true, true)
        );
    }


    /* *********************************************************************
     * Tests parametrizados para checkPlayerFinished y checkPlayerIsDead
     ********************************************************************* */
    @ParameterizedTest
    @MethodSource("checkPlayerScenarios")
    void testCheckPlayerStatus_Parametrized(List<SalmonMatch> salmonMatches,
                                        boolean expectedFinished,
                                        boolean expectedIsDead) {
        when(salmonMatchService.findAllFromPlayer(PLAYER_ID)).thenReturn(salmonMatches);
        
        boolean resultFinished = playerService.checkPlayerFinished(PLAYER_ID);
        boolean resultIsDead = playerService.checkPlayerIsDead(PLAYER_ID);
        
        assertEquals(expectedFinished, resultFinished);
        assertEquals(expectedIsDead, resultIsDead);
    }

    static Stream<Arguments> checkPlayerScenarios() {
        SalmonMatch salmon1 = mock(SalmonMatch.class);
        when(salmon1.getCoordinate()).thenReturn(new Coordinate(0, 21));
        
        SalmonMatch salmon2 = mock(SalmonMatch.class);
        when(salmon2.getCoordinate()).thenReturn(new Coordinate(0, 15));
        
        return Stream.of(
            // Caso 1: Todos los salmones terminaron
            Arguments.of(Collections.singletonList(salmon1), true, false),
            
            // Caso 2: Algunos salmones no terminaron
            Arguments.of(Arrays.asList(salmon1, salmon2), false, false),
            
            // Caso 3: Sin salmones (muerto)
            Arguments.of(Collections.emptyList(), false, true),
            
            // Caso 4: Mixto con null
            Arguments.of(Arrays.asList(salmon1, null), false, false)
        );
    }
}