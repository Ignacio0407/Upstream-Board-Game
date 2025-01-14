package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.player.Player;

@ExtendWith(MockitoExtension.class)
public class SalmonMatchRepositoryTest {

    @Mock
    private SalmonMatchRepository salmonMatchRepository;

    @Test
    public void testFindAllFromMatch() {
        Integer matchId = 1;

        SalmonMatch sm1 = new SalmonMatch();
        sm1.setMatch(new Match());
        sm1.getMatch().setId(matchId);

        List<SalmonMatch> salmonMatches = new ArrayList<>();
        salmonMatches.add(sm1);

        when(salmonMatchRepository.findAllFromMatch(matchId)).thenReturn(salmonMatches);

        List<SalmonMatch> result = salmonMatchRepository.findAllFromMatch(matchId);

        assertNotNull(result);
        assertEquals(1, result.size());

        Integer invalidMatchId = 99;

        when(salmonMatchRepository.findAllFromMatch(invalidMatchId)).thenReturn(Collections.emptyList());

        result = salmonMatchRepository.findAllFromMatch(invalidMatchId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAllFromMatchInRiver() {
        Integer matchId = 1;

        SalmonMatch sm1 = new SalmonMatch();
        sm1.setMatch(new Match());
        sm1.getMatch().setId(matchId);
        sm1.setCoordinate(new Coordinate(10, 15));

        List<SalmonMatch> salmonMatches = new ArrayList<>();
        salmonMatches.add(sm1);

        when(salmonMatchRepository.findAllFromMatchInRiver(matchId)).thenReturn(salmonMatches);

        List<SalmonMatch> result = salmonMatchRepository.findAllFromMatchInRiver(matchId);

        assertNotNull(result);
        assertEquals(1, result.size());

        Integer invalidMatchId = 99;

        when(salmonMatchRepository.findAllFromMatchInRiver(invalidMatchId)).thenReturn(Collections.emptyList());

        result = salmonMatchRepository.findAllFromMatchInRiver(invalidMatchId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAllFromPlayer() {
        Integer playerId = 1;

        SalmonMatch sm1 = new SalmonMatch();
        sm1.setPlayer(new Player());
        sm1.getPlayer().setId(playerId);

        List<SalmonMatch> salmonMatches = new ArrayList<>();
        salmonMatches.add(sm1);

        when(salmonMatchRepository.findAllFromPlayer(playerId)).thenReturn(salmonMatches);

        List<SalmonMatch> result = salmonMatchRepository.findAllFromPlayer(playerId);

        assertNotNull(result);
        assertEquals(1, result.size());

        Integer invalidPlayerId = 99;

        when(salmonMatchRepository.findAllFromPlayer(invalidPlayerId)).thenReturn(Collections.emptyList());

        result = salmonMatchRepository.findAllFromPlayer(invalidPlayerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAllFromPlayerInSea() {
        Integer playerId = 1;

        SalmonMatch sm1 = new SalmonMatch();
        sm1.setPlayer(new Player());
        sm1.getPlayer().setId(playerId);
        sm1.setCoordinate(null);

        List<SalmonMatch> salmonMatches = new ArrayList<>();
        salmonMatches.add(sm1);

        when(salmonMatchRepository.findAllFromPlayerInSea(playerId)).thenReturn(salmonMatches);

        List<SalmonMatch> result = salmonMatchRepository.findAllFromPlayerInSea(playerId);

        assertNotNull(result);
        assertEquals(1, result.size());

        Integer invalidPlayerId = 99;

        when(salmonMatchRepository.findAllFromPlayerInSea(invalidPlayerId)).thenReturn(Collections.emptyList());

        result = salmonMatchRepository.findAllFromPlayerInSea(invalidPlayerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindFromGameInSpawn() {
        Integer matchId = 1;

        SalmonMatch sm1 = new SalmonMatch();
        sm1.setMatch(new Match());
        sm1.getMatch().setId(matchId);
        sm1.setCoordinate(new Coordinate(10, 30));

        List<SalmonMatch> salmonMatches = new ArrayList<>();
        salmonMatches.add(sm1);

        when(salmonMatchRepository.findFromGameInSpawn(matchId)).thenReturn(salmonMatches);

        List<SalmonMatch> result = salmonMatchRepository.findFromGameInSpawn(matchId);

        assertNotNull(result);
        assertEquals(1, result.size());

        Integer invalidMatchId = 99;

        when(salmonMatchRepository.findFromGameInSpawn(invalidMatchId)).thenReturn(Collections.emptyList());

        result = salmonMatchRepository.findFromGameInSpawn(invalidMatchId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAllFromPlayerInRiver() {
        Integer playerId = 1;

        SalmonMatch sm1 = new SalmonMatch();
        sm1.setPlayer(new Player());
        sm1.getPlayer().setId(playerId);
        sm1.setCoordinate(new Coordinate(10, 15));

        List<SalmonMatch> salmonMatches = new ArrayList<>();
        salmonMatches.add(sm1);

        when(salmonMatchRepository.findAllFromPlayerInRiver(playerId)).thenReturn(salmonMatches);

        List<SalmonMatch> result = salmonMatchRepository.findAllFromPlayerInRiver(playerId);

        assertNotNull(result);
        assertEquals(1, result.size());

        Integer invalidPlayerId = 99;

        when(salmonMatchRepository.findAllFromPlayerInRiver(invalidPlayerId)).thenReturn(Collections.emptyList());

        result = salmonMatchRepository.findAllFromPlayerInRiver(invalidPlayerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindWithNoCoord() {
        Integer matchId = 1;

        SalmonMatch sm1 = new SalmonMatch();
        sm1.setMatch(new Match());
        sm1.getMatch().setId(matchId);
        sm1.setCoordinate(null);

        List<SalmonMatch> salmonMatches = new ArrayList<>();
        salmonMatches.add(sm1);

        when(salmonMatchRepository.findWithNoCoord(matchId)).thenReturn(salmonMatches);

        List<SalmonMatch> result = salmonMatchRepository.findWithNoCoord(matchId);

        assertNotNull(result);
        assertEquals(1, result.size());

        Integer invalidMatchId = 99;

        when(salmonMatchRepository.findWithNoCoord(invalidMatchId)).thenReturn(Collections.emptyList());

        result = salmonMatchRepository.findWithNoCoord(invalidMatchId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAllFromPlayerInSpawn() {
        Integer playerId = 1;

        SalmonMatch sm1 = new SalmonMatch();
        sm1.setPlayer(new Player());
        sm1.getPlayer().setId(playerId);
        sm1.setCoordinate(new Coordinate(10, 30));

        List<SalmonMatch> salmonMatches = new ArrayList<>();
        salmonMatches.add(sm1);

        when(salmonMatchRepository.findAllFromPlayerInSpawn(playerId)).thenReturn(salmonMatches);

        List<SalmonMatch> result = salmonMatchRepository.findAllFromPlayerInSpawn(playerId);

        assertNotNull(result);
        assertEquals(1, result.size());

        Integer invalidPlayerId = 99;

        when(salmonMatchRepository.findAllFromPlayerInSpawn(invalidPlayerId)).thenReturn(Collections.emptyList());

        result = salmonMatchRepository.findAllFromPlayerInSpawn(invalidPlayerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}