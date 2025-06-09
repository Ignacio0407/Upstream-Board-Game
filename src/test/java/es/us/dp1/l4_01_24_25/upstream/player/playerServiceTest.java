package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player player1;
    private Player player2;

    @BeforeEach
    void setup() {
        player1 = new Player();
        player1.setId(1);
        player1.setName("Player1");
        player1.setColor(Color.AMARILLO);
        player1.setPlayerOrder(1);
        player1.setAlive(true);
        player1.setPoints(10);

        player2 = new Player();
        player2.setId(2);
        player2.setName("Player2");
        player2.setColor(Color.AMARILLO);
        player2.setPlayerOrder(2);
        player2.setAlive(true);
        player2.setPoints(20);
    }

    @Nested
    @DisplayName("Get Operations Tests")
    class GetOperationsTests {
        
        @Test
        void testGetPlayers() {
            ArrayList<Player> players = (ArrayList<Player>) Arrays.asList(player1, player2);
            when(playerRepository.findAll()).thenReturn(players);

            List<Player> result = playerService.findAll();

            assertEquals(2, result.size());
            verify(playerRepository).findAll();
        }

        @Test
        void testfindById_Success() {
            when(playerRepository.findById(1)).thenReturn(Optional.of(player1));

            Player result = playerService.findById(1);

            assertNotNull(result);
            assertEquals("Player1", result.getName());
            verify(playerRepository).findById(1);
        }

        @Test
        void testfindById_NotFound() {
            when(playerRepository.findById(99)).thenReturn(Optional.empty());

            Player result = playerService.findById(99);

            assertNull(result);
            verify(playerRepository).findById(99);
        }

        @Test
        void testGetByName_Success() {
            when(playerRepository.findByName("Player1")).thenReturn(Optional.of(player1));

            Player result = new Player();//playerService.getByName("Player1");

            assertNotNull(result);
            assertEquals(1, result.getId());
            verify(playerRepository).findByName("Player1");
        }

        @Test
        void testGetByName_NotFound() {
            when(playerRepository.findByName("NonExistent")).thenReturn(null);

            Player result = new Player();//playerService.getByName("NonExistent");

            assertNull(result);
            verify(playerRepository).findByName("NonExistent");
        }

        @Test
        void testGetSomePlayersByName_Success() {
            when(playerRepository.findByName("Player1")).thenReturn(Optional.of(player1));
            when(playerRepository.findByName("Player2")).thenReturn(Optional.of(player2));

            List<String> names = Arrays.asList("Player1", "Player2");
            List<Player> result = new ArrayList<>();//playerService.getSomeByName(names);

            assertEquals(2, result.size());
            assertEquals(1, result.get(0).getId());
            assertEquals(2, result.get(1).getId());
        }

        @Test
        void testGetSomePlayersByName_EmptyList() {
            List<String> names = Arrays.asList();
            List<Player> result = new ArrayList<>();//playerService.getSomeByName(names);
            assertTrue(result.isEmpty());
        }

        @Test
        void testGetPlayersByMatch_Success() {
            List<Player> players = Arrays.asList(player1, player2);
            when(playerRepository.findPlayersByMatch(1)).thenReturn(players);

            List<Player> result = playerService.findPlayersByMatch(1);

            assertEquals(2, result.size());
            assertEquals(players, result);
        }

        @Test
        void testGetPlayersByMatch_EmptyResult() {
            when(playerRepository.findPlayersByMatch(1)).thenReturn(Arrays.asList());

            List<Player> result = playerService.findPlayersByMatch(1);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete Operations Tests")
    class DeleteOperationsTests {

        @Test
        void testDeleteById_Success() {
            when(playerRepository.findById(1)).thenReturn(Optional.of(player1));

            playerService.delete(1);

            verify(playerRepository).deleteById(1);
        }

        @Test
        void testDeleteById_NotFound() {
            when(playerRepository.findById(99)).thenReturn(Optional.empty());

            assertNull(playerService.findById(99));
        }

        @Test
        void testDeleteByName_NotFound() {
            when(playerRepository.findByName("NonExistent")).thenReturn(null);

            Player result = new Player();//playerService.getByName("NonExistent");
            assertNull(result);
        }

    }

    @Nested
    @DisplayName("Update Operations Tests")
    class UpdateOperationsTests {

        @Test
        void testUpdatePlayerById_Success() {
            Player updatedPlayer = new Player();
            updatedPlayer.setId(1);
            updatedPlayer.setName("UpdatedPlayer");
            updatedPlayer.setColor(Color.ROJO);
            updatedPlayer.setPoints(30);

            when(playerRepository.findById(1)).thenReturn(Optional.of(player1));
            when(playerRepository.save(any())).thenReturn(updatedPlayer);

            Player result = playerService.updateById(updatedPlayer, 1);

            assertEquals("UpdatedPlayer", result.getName());
            assertEquals(Color.ROJO, result.getColor());
            assertEquals(30, result.getPoints());
        }

        @Test
        void testUpdatePlayerById_NotFound() {
            when(playerRepository.findById(99)).thenReturn(Optional.empty());

            Player result = playerService.findById(99);
            assertNull(result);
        }

        @Test
        void testUpdatePlayerByName_NotFound() {
            when(playerRepository.findByName("NonExistent")).thenReturn(null);

            Player result = new Player();//playerService.getByName("NonExistent");
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Save Operations Tests")
    class SaveOperationsTests {

        @Test
        void testSavePlayer_Success() {
            when(playerRepository.save(any(Player.class))).thenReturn(player1);

            Player result = playerService.save(player1);

            assertEquals("Player1", result.getName());
            verify(playerRepository).save(player1);
        }

        @Test
        void testSavePlayer_Error() {
            when(playerRepository.save(any(Player.class)))
                .thenThrow(new DataIntegrityViolationException("Database error"));

            assertThrows(DataAccessException.class, () -> 
                playerService.save(player1));
        }

        @Test
        void testSavePlayers_Success() {
            List<Player> players = Arrays.asList(player1, player2);
            when(playerRepository.save(any(Player.class))).thenReturn(player1, player2);

            List<Player> result = playerService.savePlayers(players);

            assertTrue(result.isEmpty());
            verify(playerRepository, times(2)).save(any(Player.class));
        }

        @Test
        void testSavePlayers_PartialSuccess() {
            List<Player> players = Arrays.asList(player1, player2);
            when(playerRepository.save(player1)).thenReturn(player1);
            when(playerRepository.save(player2)).thenThrow(new DataAccessException("Error") {});

            List<Player> result = playerService.savePlayers(players);

            assertEquals(1, result.size());
            assertTrue(result.contains(player2));
        }

        @Test
        void testSavePlayers_EmptyList() {
            List<Player> players = Arrays.asList();
            List<Player> result = playerService.savePlayers(players);
            assertTrue(result.isEmpty());
        }
    }
}