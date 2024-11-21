package es.us.dp1.l4_01_24_25.upstream.matchTile;

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

import es.us.dp1.l4_01_24_25.upstream.match.Match;

@ExtendWith(MockitoExtension.class)
class matchTileServiceTest {

    @Mock
    private MatchTileRepository matchTileRepository;

    @InjectMocks
    private MatchTileService matchTileService;

    private MatchTile matchTile1;
    private MatchTile matchTile2;
    private Match partidaTest;

    @BeforeEach
    void setup() {
        partidaTest = new Match();
        partidaTest.setId(1);

        matchTile1 = new MatchTile();
        matchTile1.setId(1);
        matchTile1.setCapacity(2);
        matchTile1.setMatch(partidaTest);

        matchTile2 = new MatchTile();
        matchTile2.setId(2);
        matchTile2.setCapacity(2);
        matchTile2.setMatch(partidaTest);
    }

    @Nested
    @DisplayName("GET Operations Tests")
    class GetOperationsTests {
        
        @Test
        void testGetAllMatchTiles() {
            // Given
            List<MatchTile> expectedmatchTiles = Arrays.asList(matchTile2, matchTile1);
            when(matchTileRepository.findAll()).thenReturn(expectedmatchTiles);

            // When
            List<MatchTile> result = matchTileService.findAll();

            // Then
            assertEquals(expectedmatchTiles, result);
            verify(matchTileRepository).findAll();
        }

        @Test
        void testGetMatchTileById_Success() {
            // Given
            when(matchTileRepository.findById(1)).thenReturn(Optional.of(matchTile1));

            // When
            MatchTile result = matchTileService.findById(1);

            // Then
            assertNotNull(result);
            assertEquals(matchTile1.getId(), result.getId());
            verify(matchTileRepository).findById(1);
        }

        @Test
        void testGetMatchTileById_NotFound() {
            // Given
            when(matchTileRepository.findById(99)).thenReturn(Optional.empty());

            // When
            MatchTile result = matchTileService.findById(99);

            // Then
            assertNull(result);
            verify(matchTileRepository).findById(99);
        }

        @Test
        void testGetMatchTileByGameId_Success() {
            // Given
            List<MatchTile> expectedmatchTiles = Arrays.asList(matchTile2, matchTile1);
            when(matchTileRepository.findByMatchId(1)).thenReturn(expectedmatchTiles);

            // When
            List<MatchTile> result = matchTileService.findByMatchId(1);

            // Then
            assertNotNull(result);
            assertEquals(expectedmatchTiles, result);
            verify(matchTileRepository).findByMatchId(1);
        }

        @Test
        void testGetMatchTileBygameId_NotFound() {
            // Given
            when(matchTileRepository.findByMatchId(99)).thenReturn(null);

            // When
            List<MatchTile> result = matchTileService.findByMatchId(99);

            // Then
            assertNull(result);
            verify(matchTileRepository).findByMatchId(99);
        }
    }

    @Nested
    @DisplayName("POST Operations Tests")
    class PostOperationsTests {
        
        @Test
        void testSaveMatchTile_Success() {
            // Given
            when(matchTileRepository.save(any(MatchTile.class))).thenReturn(matchTile1);

            // When
            MatchTile result = matchTileService.save(matchTile1);

            // Then
            assertNotNull(result);
            assertEquals(matchTile1.getId(), result.getId());
            verify(matchTileRepository).save(matchTile1);
        }
    }

    /*
    @Nested
    @DisplayName("PUT Operations Tests")
    class PutOperationsTests {
    }
     */

    @Nested
    @DisplayName("DELETE Operations Tests")
    class DeleteOperationsTests {

        @Test
        void testDeleteMatchTileById_Success() {
            // Given
            when(matchTileRepository.findById(1)).thenReturn(Optional.of(matchTile1));

            // When
            matchTileService.deleteMatchTile(1);

            // Then
            verify(matchTileRepository).delete(matchTile1);
        }
    }
}