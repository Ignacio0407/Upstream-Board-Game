package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
public class MatchTileServiceTest {

    @Mock
    private MatchTileRepository matchTileRepository;
    
    @Mock
    private TileService tileService;

    @InjectMocks
    private MatchTileService matchTileService;

    private MatchTile matchTile1;
    private MatchTile matchTile2;
    private Match testMatch;
    private Tile waterTile;

    @BeforeEach
    void setup() {
        testMatch = new Match();
        testMatch.setId(1);

        waterTile = new Tile();
        waterTile.setId(1);

        matchTile1 = new MatchTile();
        matchTile1.setId(1);
        matchTile1.setCapacity(2);
        matchTile1.setMatch(testMatch);
        matchTile1.setCoordinate(new Coordinate(1,1));
        matchTile1.setOrientation(1);
        matchTile1.setSalmonsNumber(2);

        matchTile2 = new MatchTile();
        matchTile2.setId(2);
        matchTile2.setCapacity(2);
        matchTile2.setMatch(testMatch);
    }

    @Nested
    @DisplayName("GET Operations Tests")
    class GetOperationsTests {
        
        @Test
        void testGetAllMatchTiles() {
            List<MatchTile> expectedMatchTiles = Arrays.asList(matchTile2, matchTile1);
            //when(matchTileRepository.findAll()).thenReturn(expectedMatchTiles);

            List<MatchTile> result = matchTileService.findAll();

            assertEquals(expectedMatchTiles, result);
            verify(matchTileRepository).findAll();
        }

        @Test
        void testGetMatchTileById_Success() {
            when(matchTileRepository.findById(1)).thenReturn(Optional.of(matchTile1));

            MatchTile result = matchTileService.findById(1);

            assertNotNull(result);
            assertEquals(matchTile1.getId(), result.getId());
            verify(matchTileRepository).findById(1);
        }

        @Test
        void testGetMatchTileById_NotFound() {
            when(matchTileRepository.findById(99)).thenReturn(Optional.empty());

            MatchTile result = matchTileService.findById(99);

            assertNull(result);
            verify(matchTileRepository).findById(99);
        }

        @Test
        void testGetMatchTileByGameId_Success() {
            List<MatchTile> expectedMatchTiles = Arrays.asList(matchTile2, matchTile1);
            when(matchTileRepository.findByMatchId(1)).thenReturn(expectedMatchTiles);

            List<MatchTile> result = matchTileService.findByMatchId(1);

            assertNotNull(result);
            assertEquals(expectedMatchTiles, result);
            verify(matchTileRepository).findByMatchId(1);
        }

        @Test
        void testGetMatchTileByGameId_NotFound() {
            when(matchTileRepository.findByMatchId(99)).thenReturn(null);

            List<MatchTile> result = matchTileService.findByMatchId(99);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(matchTileRepository).findByMatchId(99);
        }

        @Test
        void testFindByMatchIdNoCoord_Success() {
            List<MatchTile> expectedMatchTiles = Arrays.asList(matchTile2);
            when(matchTileRepository.findWithNoCoord(1)).thenReturn(expectedMatchTiles);

            List<MatchTile> result = matchTileService.findByMatchIdNoCoord(1);

            assertEquals(expectedMatchTiles, result);
            verify(matchTileRepository).findWithNoCoord(1);
        }

        @Test
        void testFindByCoordinate_Success() {
            when(matchTileRepository.findByCoordinate(1, 1)).thenReturn(matchTile1);

            MatchTile result = matchTileService.findByCoordinate(1, 1);

            assertNotNull(result);
            assertEquals(matchTile1, result);
            verify(matchTileRepository).findByCoordinate(1, 1);
        }
    }

    @Nested
    @DisplayName("POST Operations Tests")
    class PostOperationsTests {
        
        @Test
        void testSaveMatchTile_Success() {
            when(matchTileRepository.save(any(MatchTile.class))).thenReturn(matchTile1);

            MatchTile result = matchTileService.save(matchTile1);

            assertNotNull(result);
            assertEquals(matchTile1.getId(), result.getId());
            verify(matchTileRepository).save(matchTile1);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        void testValidateTilePlacement_Valid() {
            boolean result = matchTileService.validateTilePlacement(3, 2);
            assertTrue(result);
        }

        @Test
        void testValidateTilePlacement_Invalid() {
            boolean result = matchTileService.validateTilePlacement(2, 3);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Transformation Tests")
    class TransformationTests {

        @Test
        void testEagleToWater_Success() {
            //when(tileService.findById(1)).thenReturn(Optional.of(waterTile));
            when(matchTileRepository.save(any(MatchTile.class))).thenAnswer(invocation -> invocation.getArgument(0));

            MatchTile result = matchTileService.eagleToWater(matchTile1, testMatch);

            assertNotNull(result);
            assertEquals(matchTile1.getId(), result.getId());
            assertEquals(matchTile1.getCapacity(), result.getCapacity());
            assertEquals(0, result.getOrientation());
            assertEquals(0, result.getSalmonsNumber());
            assertEquals(waterTile, result.getTile());
            assertEquals(testMatch, result.getMatch());
            verify(matchTileRepository).save(any(MatchTile.class));
            verify(tileService).findById(1);
        }

        @Test
        void testEagleToWater_WaterTileNotFound() {
           // when(tileService.findById(1)).thenReturn(Optional.empty());
            when(matchTileRepository.save(any(MatchTile.class))).thenAnswer(invocation -> invocation.getArgument(0));

            MatchTile result = matchTileService.eagleToWater(matchTile1, testMatch);

            assertNotNull(result);
            assertNull(result.getTile());
            verify(matchTileRepository).save(any(MatchTile.class));
            verify(tileService).findById(1);
        }
    }

    @Nested
    @DisplayName("DELETE Operations Tests")
    class DeleteOperationsTests {

        @Test
        void testDeleteMatchTileById_Success() {
            when(matchTileRepository.findById(1)).thenReturn(Optional.of(matchTile1));

            matchTileService.delete(1);

            verify(matchTileRepository).delete(matchTile1);
        }
    }
}