package es.us.dp1.l4_01_24_25.upstream.casilla;

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

import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileRepository;
import es.us.dp1.l4_01_24_25.upstream.tile.TileService;

@ExtendWith(MockitoExtension.class)
class casillaServiceTest {

    @Mock
    private TileRepository casillaRepository;

    @InjectMocks
    private TileService casillaService;

    private Tile casilla1;
    private Tile casilla2;

    @BeforeEach
    void setup() {

        casilla1 = new Tile();
        casilla1.setId(1);

        casilla2 = new Tile();
        casilla2.setId(2);
    }

    @Nested
    @DisplayName("GET Operations Tests")
    class GetOperationsTests {
        
        @Test
        void testGetAllCasillas() {
            // Given
            List<Tile> expectedCasillas = Arrays.asList(casilla1, casilla2);
            when(casillaRepository.findAll()).thenReturn(expectedCasillas);

            // When
            List<Tile> result = casillaService.findAll();

            // Then
            assertEquals(expectedCasillas, result);
            verify(casillaRepository).findAll();
        }

        @Test
        void testGetCasillaById_Success() {
            // Given
            when(casillaRepository.findById(1)).thenReturn(Optional.of(casilla1));

            // When
            Optional<Tile> result = casillaService.findById(1);

            // Then
            assertNotNull(result);
            assertEquals(casilla1.getId(), result.get().getId());
            verify(casillaRepository).findById(1);
        }
    }
}