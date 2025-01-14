package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.player.Player;

@ExtendWith(MockitoExtension.class)
class MatchRepositoryTest {

    @Mock
    private MatchRepository matchRepository;

    @Test
    void findPlayersFromGame_shouldReturnPlayerList_whenMatchExists() {
        
        Integer matchId = 1;
        List<Player> expectedPlayers = new ArrayList<>();
        Player player1 = new Player();
        Player player2 = new Player();
        expectedPlayers.add(player1);
        expectedPlayers.add(player2);
        
        when(matchRepository.findPlayersFromGame(matchId)).thenReturn(expectedPlayers);

        List<Player> result = matchRepository.findPlayersFromGame(matchId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedPlayers, result);
    }


    @Test
    void findPlayersFromGame_shouldReturnEmptyList_whenMatchDoesNotExist() {

        Integer matchId = 999;
        when(matchRepository.findPlayersFromGame(matchId)).thenReturn(new ArrayList<>());

        List<Player> result = matchRepository.findPlayersFromGame(matchId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void findHeronWithCoordFromGame_shouldReturnMatchTileList_whenMatchExists() {
        Integer matchId = 1;
        List<MatchTile> expectedTiles = new ArrayList<>();
        MatchTile tile1 = new MatchTile();
        MatchTile tile2 = new MatchTile();
        expectedTiles.add(tile1);
        expectedTiles.add(tile2);
        
        when(matchRepository.findHeronWithCoordFromGame(matchId)).thenReturn(expectedTiles);

        List<MatchTile> result = matchRepository.findHeronWithCoordFromGame(matchId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedTiles, result);
    }


    @Test
    void findHeronWithCoordFromGame_shouldReturnEmptyList_whenNoHeronsFound() {

        Integer matchId = 1;
        when(matchRepository.findHeronWithCoordFromGame(matchId)).thenReturn(new ArrayList<>());

        List<MatchTile> result = matchRepository.findHeronWithCoordFromGame(matchId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
