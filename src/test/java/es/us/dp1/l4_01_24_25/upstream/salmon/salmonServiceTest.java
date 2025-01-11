package es.us.dp1.l4_01_24_25.upstream.salmon;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.dao.DataIntegrityViolationException;

import es.us.dp1.l4_01_24_25.upstream.player.Color;

@ExtendWith(MockitoExtension.class)
class SalmonServiceTest {

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
        salmon1.setColor(Color.ROJO);
        salmon1.setImage("salmon1.png");
        
        salmon2 = new Salmon();
        salmon2.setId(2);
        salmon2.setColor(Color.AMARILLO);
        salmon2.setImage("salmon2.png");
    }

    @Nested
    @DisplayName("GET Operations Tests")
    class GetOperationsTests {
        
        @Test
        @DisplayName("Should find all salmons successfully")
        void testGetAllSalmons() {
            // Given
            List<Salmon> expectedSalmons = Arrays.asList(salmon1, salmon2);
            when(salmonRepository.findAll()).thenReturn(expectedSalmons);

            // When
            List<Salmon> result = salmonService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(expectedSalmons, result);
            verify(salmonRepository).findAll();
        }

        @Test
        @DisplayName("Should find all salmons when empty")
        void testGetAllSalmons_EmptyList() {
            // Given
            when(salmonRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<Salmon> result = salmonService.findAll();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(salmonRepository).findAll();
        }

        @Test
        @DisplayName("Should find salmon by ID successfully")
        void testGetSalmonById_Success() {
            // Given
            when(salmonRepository.findById(1)).thenReturn(Optional.of(salmon1));

            // When
            Optional<Salmon> result = salmonService.findById(1);

            // Then
            assertTrue(result.isPresent());
            assertEquals(salmon1.getId(), result.get().getId());
            assertEquals(salmon1.getColor(), result.get().getColor());
            assertEquals(salmon1.getImage(), result.get().getImage());
            verify(salmonRepository).findById(1);
        }

        @Test
        @DisplayName("Should return empty Optional when salmon not found")
        void testGetSalmonById_NotFound() {
            // Given
            when(salmonRepository.findById(99)).thenReturn(Optional.empty());

            // When
            Optional<Salmon> result = salmonService.findById(99);

            // Then
            assertFalse(result.isPresent());
            verify(salmonRepository).findById(99);
        }
    }

    @Nested
    @DisplayName("CREATE Operations Tests")
    class CreateOperationsTests {

        @Test
        @DisplayName("Should create salmon successfully")
        void testCreateSalmon_Success() {
            // Given
            when(salmonRepository.save(any(Salmon.class))).thenReturn(salmon1);

            // When
            Salmon result = salmonService.create(salmon1);

            // Then
            assertNotNull(result);
            assertEquals(salmon1.getId(), result.getId());
            assertEquals(salmon1.getColor(), result.getColor());
            assertEquals(salmon1.getImage(), result.getImage());
            verify(salmonRepository).save(salmon1);
        }

        @Test
        @DisplayName("Should throw exception when creating salmon with invalid data")
        void testCreateSalmon_Error() {
            // Given
            when(salmonRepository.save(any(Salmon.class)))
                .thenThrow(new DataIntegrityViolationException("Database error"));

            // Then
            assertThrows(DataIntegrityViolationException.class, () -> {
                // When
                salmonService.create(salmon1);
            });
            verify(salmonRepository).save(salmon1);
        }

        @Test
        @DisplayName("Should create salmon with null ID")
        void testCreateSalmon_WithNullId() {
            // Given
            Salmon newSalmon = new Salmon();
            newSalmon.setColor(Color.VERDE);
            newSalmon.setImage("newSalmon.png");
            
            Salmon savedSalmon = new Salmon();
            savedSalmon.setId(3);
            savedSalmon.setColor(Color.VERDE);
            savedSalmon.setImage("newSalmon.png");
            
            when(salmonRepository.save(any(Salmon.class))).thenReturn(savedSalmon);

            // When
            Salmon result = salmonService.create(newSalmon);

            // Then
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(savedSalmon.getColor(), result.getColor());
            assertEquals(savedSalmon.getImage(), result.getImage());
            verify(salmonRepository).save(newSalmon);
        }
    }
}