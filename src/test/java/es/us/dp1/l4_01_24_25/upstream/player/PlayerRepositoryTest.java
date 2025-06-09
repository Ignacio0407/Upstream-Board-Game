package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.l4_01_24_25.upstream.match.Match;

@ExtendWith(MockitoExtension.class)
public class PlayerRepositoryTest {

    @Mock
    private PlayerRepository playerRepository;

    @Test
    public void testFindPlayersByMatch() {
        Integer matchId = 1;

        Player player1 = new Player();
        player1.setName("Player 1");
        player1.setMatch(new Match());
        player1.getMatch().setId(matchId);

        List<Player> players = new ArrayList<>();
        players.add(player1);

        when(playerRepository.findPlayersByMatch(matchId)).thenReturn(players);

        List<Player> result = playerRepository.findPlayersByMatch(matchId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Player 1", result.get(0).getName());

        Integer invalidMatchId = 99;

        when(playerRepository.findPlayersByMatch(invalidMatchId)).thenReturn(Collections.emptyList());

        result = playerRepository.findPlayersByMatch(invalidMatchId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAlivePlayersByMatch() {
        Integer matchId = 1;

        Player alivePlayer = new Player();
        alivePlayer.setName("Alive Player");
        alivePlayer.setAlive(true);
        alivePlayer.setMatch(new Match());
        alivePlayer.getMatch().setId(matchId);

        List<Player> alivePlayers = new ArrayList<>();
        alivePlayers.add(alivePlayer);

        when(playerRepository.findAlivePlayersByMatch(matchId)).thenReturn(alivePlayers);

        List<Player> result = playerRepository.findAlivePlayersByMatch(matchId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getAlive());

        Integer invalidMatchId = 99;

        when(playerRepository.findAlivePlayersByMatch(invalidMatchId)).thenReturn(Collections.emptyList());

        result = playerRepository.findAlivePlayersByMatch(invalidMatchId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByName() {
        String name = "Player 1";

        Player player = new Player();
        player.setName(name);

        when(playerRepository.findByName(name)).thenReturn(Optional.of(player));

        Optional<Player> result = playerRepository.findByName(name);

        assertNotNull(result);
        assertEquals(name, result.get().getName());

        String invalidName = "Non Existent Player";

        when(playerRepository.findByName(invalidName)).thenReturn(null);

        result = playerRepository.findByName(invalidName);

        assertNull(result);
    }
}