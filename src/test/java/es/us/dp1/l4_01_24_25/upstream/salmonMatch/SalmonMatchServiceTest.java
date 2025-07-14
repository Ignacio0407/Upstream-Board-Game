package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.player.Player;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
public class SalmonMatchServiceTest {

    @Mock
    private SalmonMatchRepository salmonMatchRepository;

    @InjectMocks
    private SalmonMatchService salmonMatchService;

    private SalmonMatch salmonMatch1;
    private SalmonMatch salmonMatch2;
    private Match match1;
    private Player player1;

    @BeforeEach
    void setup() {
        match1 = new Match();
        match1.setId(1);

        player1 = new Player();
        player1.setId(1);

        salmonMatch1 = new SalmonMatch();
        salmonMatch1.setId(1);
        salmonMatch1.setMatch(match1);
        salmonMatch1.setPlayer(player1);

        salmonMatch2 = new SalmonMatch();
        salmonMatch2.setId(2);
        salmonMatch2.setMatch(match1);
        salmonMatch2.setPlayer(player1);
    }

    @Nested
    @DisplayName("Save Operations Tests")
    class SaveOperationsTests {
        
        @Test
        @DisplayName("Should save salmon match successfully")
        void testSaveSuccess() {
            when(salmonMatchRepository.save(salmonMatch1)).thenReturn(salmonMatch1);

            SalmonMatch result = salmonMatchService.save(salmonMatch1);

            assertNotNull(result);
            assertEquals(salmonMatch1, result);
            verify(salmonMatchRepository).save(salmonMatch1);
        }

        @Test
        @DisplayName("Should throw exception when save fails")
        void testSaveFail() {
            when(salmonMatchRepository.save(salmonMatch1))
                .thenThrow(new DataAccessException("Test exception") {});

            assertThrows(DataAccessException.class, () -> salmonMatchService.save(salmonMatch1));
            verify(salmonMatchRepository).save(salmonMatch1);
        }
    }

    @Nested
    @DisplayName("Get Operations Tests")
    class GetOperationsTests {

        @Test
        @DisplayName("Should get salmon match by ID successfully")
        void testfindById_Success() {
            when(salmonMatchRepository.findById(1)).thenReturn(Optional.of(salmonMatch1));

            SalmonMatch result = salmonMatchService.findById(1);

            assertNotNull(result);
            assertEquals(salmonMatch1, result);
            verify(salmonMatchRepository).findById(1);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when salmon match not found")
        void testfindById_NotFound() {
            when(salmonMatchRepository.findById(99)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> salmonMatchService.findById(99));
            verify(salmonMatchRepository).findById(99);
        }

        @Test
        @DisplayName("Should get all salmon matches from match successfully")
        void testGetAllFromMatch_Success() {
            List<SalmonMatch> expected = List.of(salmonMatch1, salmonMatch2);
            when(salmonMatchRepository.findAllFromMatch(1)).thenReturn(expected);

            List<SalmonMatch> result = salmonMatchService.findAllFromMatch(1);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(salmonMatchRepository).findAllFromMatch(1);
        }

        @Test
        @DisplayName("Should return empty list when no salmon matches found for match")
        void testGetAllFromMatch_Empty() {
            when(salmonMatchRepository.findAllFromMatch(1)).thenReturn(null);

            List<SalmonMatch> result = salmonMatchService.findAllFromMatch(1);

            assertEquals(List.of(), result);
            verify(salmonMatchRepository).findAllFromMatch(1);
        }

        @Test
        @DisplayName("Should get all salmon matches from player successfully")
        void testGetAllFromPlayer_Success() {
            List<SalmonMatch> expected = List.of(salmonMatch1, salmonMatch2);
            when(salmonMatchRepository.findAllFromPlayer(1)).thenReturn(expected);

            List<SalmonMatch> result = salmonMatchService.findAllFromPlayer(1);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(salmonMatchRepository).findAllFromPlayer(1);
        }
    }

    @Nested
    @DisplayName("Delete Operations Tests")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should delete salmon match successfully")
        void testDelete_Success() {
            salmonMatchService.delete(1);
            verify(salmonMatchRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should throw exception when delete fails")
        void testDelete_Fail() {
            doThrow(new DataAccessException("Test Exception") {})
                .when(salmonMatchRepository).deleteById(1);

            assertThrows(DataAccessException.class, () -> salmonMatchService.delete(1));
            verify(salmonMatchRepository).deleteById(1);
        }
    }

    @Nested
    @DisplayName("Spawn and Sea Operations Tests")
    class SpawnAndSeaOperationsTests {

        @Test
        @DisplayName("Should get salmons in spawn from game")
        void testGetSalmonsInSpawnFromGame() {
            List<SalmonMatch> expected = List.of(salmonMatch1);
            when(salmonMatchRepository.findFromGameInSpawn(1)).thenReturn(expected);

            List<SalmonMatch> result = salmonMatchService.findFromGameInSpawn(1);

            assertEquals(expected, result);
            verify(salmonMatchRepository).findFromGameInSpawn(1);
        }

        @Test
        @DisplayName("Should get salmons in sea")
        void testGetSalmonsInSea() {
            List<SalmonMatch> expected = List.of(salmonMatch2);
            when(salmonMatchRepository.findByMatchIdNoCoord(1)).thenReturn(expected);

            List<SalmonMatch> result = salmonMatchService.findSalmonsInSea(1);

            assertEquals(expected, result);
            verify(salmonMatchRepository).findByMatchIdNoCoord(1);
        }

        @Test
        @DisplayName("Should get salmons from player in spawn")
        void testGetSalmonsFromPlayerInSpawn() {
            List<SalmonMatch> expected = List.of(salmonMatch1);
            when(salmonMatchRepository.findAllFromPlayerInSpawn(1)).thenReturn(expected);

            List<SalmonMatch> result = salmonMatchService.findSalmonsFromPlayerInSpawn(1);

            assertEquals(expected, result);
            verify(salmonMatchRepository).findAllFromPlayerInSpawn(1);
        }

        @Test
        @DisplayName("Should calculate points for salmon in spawn")
        void testGetPointsFromASalmonInSpawn() {
            SalmonMatch salmonInSpawn = new SalmonMatch();
            salmonInSpawn.setCoordinate(new Coordinate(5, 25)); // y > 20
            when(salmonMatchRepository.findById(1)).thenReturn(Optional.of(salmonInSpawn));

            Integer points = salmonMatchService.findPointsFromASalmonInSpawn(1);

            assertEquals(5, points); // 25 - 20 = 5 points
            verify(salmonMatchRepository).findById(1);
        }

        @Test
        @DisplayName("Should return 0 points for salmon not high enough in spawn")
        void testGetPointsFromASalmonInSpawn_NoPoints() {
            SalmonMatch salmonInSpawn = new SalmonMatch();
            salmonInSpawn.setCoordinate(new Coordinate(5, 15)); // y < 20
            when(salmonMatchRepository.findById(1)).thenReturn(Optional.of(salmonInSpawn));

            Integer points = salmonMatchService.findPointsFromASalmonInSpawn(1);

            assertEquals(0, points);
            verify(salmonMatchRepository).findById(1);
        }
    }
}