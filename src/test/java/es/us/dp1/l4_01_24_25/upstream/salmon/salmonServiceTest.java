package es.us.dp1.l4_01_24_25.upstream.salmon;

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
class salmonServiceTest {

    @Mock
    private SalmonRepository salmonRepository;

    @InjectMocks
    private SalmonService salmonService;

    private Salmon salmon1;
    private Salmon salmon2;

    @BeforeEach
    void setup() {

        salmon1 = new Salmon();
        salmon1.setId(1);

        salmon2 = new Salmon();
        salmon2.setId(2);
    }

    @Nested
    @DisplayName("GET Operations Tests")
    class GetOperationsTests {
        
        @Test
        void testGetAllSalmons() {
            // Given
            List<Salmon> expectedSalmons = Arrays.asList(salmon1, salmon2);
            when(salmonRepository.findAll()).thenReturn(expectedSalmons);

            // When
            List<Salmon> result = salmonService.findAll();

            // Then
            assertEquals(expectedSalmons, result);
            verify(salmonRepository).findAll();
        }

        @Test
        void testGetSalmonById_Success() {
            // Given
            when(salmonRepository.findById(1)).thenReturn(Optional.of(salmon1));

            // When
            Optional<Salmon> result = salmonService.findById(1);

            // Then
            assertNotNull(result);
            assertEquals(salmon1.getId(), result.get().getId());
            verify(salmonRepository).findById(1);
        }
    }
}