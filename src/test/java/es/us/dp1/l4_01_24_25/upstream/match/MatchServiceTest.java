package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileRepository;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerRepository;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchTileRepository matchTileRepository;

    @Mock
    private SalmonMatchRepository salmonMatchRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private MatchService matchService;

    private Match match1;
    private Match match2;

    @BeforeEach
    void setup() {
        match1 = new Match();
        match1.setId(1);
        match1.setName("Match1");
        match1.setPassword("password1");
        match1.setState(State.ESPERANDO);
        match1.setPlayersNumber(2);
        match1.setRound(1);
        match1.setPhase(Phase.CASILLAS);

        match2 = new Match();
        match2.setId(2);
        match2.setName("Match2");
        match2.setPassword("password2");
        match2.setState(State.EN_CURSO);
        match2.setPlayersNumber(3);
        match2.setRound(2);
        match2.setPhase(Phase.MOVIENDO);
    }

    @Nested
    @DisplayName("GET Operations Tests")
    class GetOperationsTests {
        
        @Test
        void testGetAllMatches() {
            List<Match> expectedMatches = Arrays.asList(match1, match2);
            //when(matchRepository.findAll()).thenReturn(expectedMatches);

            List<Match> result = matchService.findAll();

            assertEquals(expectedMatches, result);
            verify(matchRepository).findAll();
        }

        @Test
        void testGetMatchById_Success() {
            when(matchRepository.findById(1)).thenReturn(Optional.of(match1));

            Match result = matchService.findById(1);

            assertNotNull(result);
            assertEquals(match1.getName(), result.getName());
            verify(matchRepository).findById(1);
        }

        @Test
        void testGetMatchById_NotFound() {
            when(matchRepository.findById(99)).thenReturn(Optional.empty());

            Match result = matchService.findById(99);

            assertNull(result);
            verify(matchRepository).findById(99);
        }

        @Test
        void testGetMatchByName_Success() {
            //when(matchRepository.findByName("Match1")).thenReturn(match1);

            Match result = new Match();// matchService.findByName("Match1");

            assertNotNull(result);
            assertEquals(match1.getName(), result.getName());
            verify(matchRepository).findByName("Match1");
        }

        @Test
        void testGetMatchByName_NotFound() {
            when(matchRepository.findByName("NonExistent")).thenReturn(null);

            Match result = new Match();//matchService.getByName("NonExistent");

            assertNull(result);
            verify(matchRepository).findByName("NonExistent");
        }

        @Test
        void testGetPlayersFromMatch_Success() {
            Player player1 = new Player();
            player1.setId(1);
            Player player2 = new Player();
            player2.setId(2);

            List<Player> players = List.of(player1, player2);
            when(matchRepository.findPlayersFromGame(1)).thenReturn(players);

            List<Player> result = matchService.findPlayersFromGame(1);

            assertEquals(2, result.size());
            assertEquals(players, result);
            verify(matchRepository).findPlayersFromGame(1);
        }

        @Test
        void testGetPlayersFromMatch_Empty() {
            when(matchRepository.findPlayersFromGame(99)).thenReturn(List.of());

            List<Player> result = matchService.findPlayersFromGame(99);

            assertEquals(0, result.size());
            verify(matchRepository).findPlayersFromGame(99);
        }
    }

    @Nested
    @DisplayName("POST Operations Tests")
    class PostOperationsTests {
        
        @Test
        void testSaveMatch() {
            when(matchRepository.save(any(Match.class))).thenReturn(match1);

            Match result = matchService.save(match1);

            assertNotNull(result);
            assertEquals(match1.getName(), result.getName());
            verify(matchRepository).save(match1);
        }
    }

    @Nested
    @DisplayName("PUT Operations Tests")
    class PutOperationsTests {
        
        @Test
        void testUpdateMatchById_Success() {
            Match updatedMatch = new Match();
            updatedMatch.setName("UpdatedMatch");

            when(matchRepository.findById(1)).thenReturn(Optional.of(match1));
            when(matchRepository.save(any(Match.class))).thenReturn(updatedMatch);

            Match result = matchService.updateById(updatedMatch, 1);

            assertNotNull(result);
            assertEquals("UpdatedMatch", result.getName());
            verify(matchRepository).save(any(Match.class));
        }

        @Test
        void testUpdateMatchById_NotFound() {
            Match updatedMatch = new Match();
            updatedMatch.setName("UpdatedMatch");

            when(matchRepository.findById(99)).thenReturn(Optional.empty());

            Match result = matchService.updateById(updatedMatch, 99);

            assertNull(result);
            verify(matchRepository).findById(99);
        }
    }

    @Nested
    @DisplayName("DELETE Operations Tests")
    class DeleteOperationsTests {
        
        @Test
        void testDeleteAllMatches() {
            //matchService.deleteAll();

            verify(matchRepository).deleteAll();
        }

        @Test
        void testDeleteMatchById_Success() {
            when(matchRepository.findById(1)).thenReturn(Optional.of(match1));

            matchService.delete(1);

            verify(matchRepository).deleteById(1);
        }

        @Test
        void testDeleteSomeMatchesById() {
            List<Integer> ids = List.of(1, 2);

            when(matchRepository.findById(1)).thenReturn(Optional.of(match1));
            when(matchRepository.findById(2)).thenReturn(Optional.of(match2));

            //matchService.deleteSomeById(ids);

            verify(matchRepository).deleteById(1);
            verify(matchRepository).deleteById(2);
        }
    }

    @Nested
    @DisplayName("Custom Methods Tests")
    class CustomMethodsTests {
        
        @Test
        void testCheckGameHasFinished() {
            when(matchRepository.findById(1)).thenReturn(Optional.of(match1));
            when(matchTileRepository.findByMatchId(1)).thenReturn(List.of());
            when(salmonMatchRepository.findAllFromMatch(1)).thenReturn(List.of());
            when(playerRepository.findPlayersByMatch(1)).thenReturn(List.of());

            matchService.checkGameHasFinished(1);

            assertEquals(State.FINALIZADA, match1.getState());
            verify(matchRepository).save(match1);
        }

        @Test
        void testChangePlayerTurn() {
            Player player = new Player();
            player.setId(1);
            player.setPlayerOrder(0);
            player.setEnergy(5);
            player.setAlive(true);
            match1.setActualPlayer(player);
            player.setMatch(match1);

            when(playerRepository.findById(1)).thenReturn(Optional.of(player));
            when(playerRepository.findAlivePlayersByMatch(1)).thenReturn(List.of(player));

            matchService.changePlayerTurn(1);

            assertNotNull(match1.getActualPlayer());
            verify(matchRepository).save(match1);
        }
    }
}