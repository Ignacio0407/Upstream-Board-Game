package es.us.dp1.l4_01_24_25.upstream.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class TileServiceTest {

    @Mock
    private TileRepository tileRepository;

    @InjectMocks
    private TileService tileService;

    private Tile tile1;
    private Tile tile2;

    @BeforeEach
    void setup() {

        tile1 = new Tile();
        tile1.setId(1);

        tile2 = new Tile();
        tile2.setId(2);
    }

    @Nested
    @DisplayName("GET Operations Tests")
    class GetOperationsTests {
        
        @Test
        void testGetAllCasillas() {
            // Given
            ArrayList<Tile> expectedCasillas = (ArrayList<Tile>) Arrays.asList(tile1, tile2);
            when(tileRepository.findAll()).thenReturn(expectedCasillas);

            // When
            List<Tile> result = tileService.findAll();

            // Then
            assertEquals(expectedCasillas, result);
            verify(tileRepository).findAll();
        }

        @Test
        void testGetCasillaById_Success() {
            // Given
            when(tileRepository.findById(1)).thenReturn(Optional.of(tile1));

            // When
            Tile result = tileService.findById(1);

            // Then
            assertEquals(tile1.getId(), result.getId());
            verify(tileRepository).findById(1);
        }
    }
}