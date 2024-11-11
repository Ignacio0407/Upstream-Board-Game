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

@ExtendWith(MockitoExtension.class)
class casillaServiceTest {

    @Mock
    private CasillaRepository casillaRepository;

    @InjectMocks
    private CasillaService casillaService;

    private Casilla casilla1;
    private Casilla casilla2;

    @BeforeEach
    void setup() {

        casilla1 = new Casilla();
        casilla1.setId(1);

        casilla2 = new Casilla();
        casilla2.setId(2);
    }

    @Nested
    @DisplayName("GET Operations Tests")
    class GetOperationsTests {
        
        @Test
        void testGetAllCasillas() {
            // Given
            List<Casilla> expectedCasillas = Arrays.asList(casilla1, casilla2);
            when(casillaRepository.findAll()).thenReturn(expectedCasillas);

            // When
            List<Casilla> result = casillaService.findAll();

            // Then
            assertEquals(expectedCasillas, result);
            verify(casillaRepository).findAll();
        }

        @Test
        void testGetCasillaById_Success() {
            // Given
            when(casillaRepository.findById(1)).thenReturn(Optional.of(casilla1));

            // When
            Optional<Casilla> result = casillaService.findById(1);

            // Then
            assertNotNull(result);
            assertEquals(casilla1.getId(), result.get().getId());
            verify(casillaRepository).findById(1);
        }
    }
}