package es.us.dp1.l4_01_24_25.upstream.partida;

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

@ExtendWith(MockitoExtension.class)
class PartidaServiceTest {

    @Mock
    private PartidaRepository partidaRepository;

    @InjectMocks
    private PartidaService partidaService;

    private Partida partida1;
    private Partida partida2;

    @BeforeEach
    void setup() {
        partida1 = new Partida();
        partida1.setId(1);
        partida1.setName("Partida1");

        partida2 = new Partida();
        partida2.setId(2);
        partida2.setName("Partida2");
    }

    @Nested
    @DisplayName("GET Operations Tests")
    class GetOperationsTests {
        
        @Test
        void testGetAllPartidas() {
            // Given
            List<Partida> expectedPartidas = Arrays.asList(partida1, partida2);
            when(partidaRepository.findAll()).thenReturn(expectedPartidas);

            // When
            List<Partida> result = partidaService.getPartidas();

            // Then
            assertEquals(expectedPartidas, result);
            verify(partidaRepository).findAll();
        }

        @Test
        void testGetPartidaById_Success() {
            // Given
            when(partidaRepository.findById(1)).thenReturn(Optional.of(partida1));

            // When
            Partida result = partidaService.getPartidaById(1);

            // Then
            assertNotNull(result);
            assertEquals(partida1.getName(), result.getName());
            verify(partidaRepository).findById(1);
        }

        @Test
        void testGetPartidaById_NotFound() {
            // Given
            when(partidaRepository.findById(99)).thenReturn(Optional.empty());

            // When
            Partida result = partidaService.getPartidaById(99);

            // Then
            assertNull(result);
            verify(partidaRepository).findById(99);
        }

        @Test
        void testGetPartidaByName_Success() {
            // Given
            when(partidaRepository.findByName("Partida1")).thenReturn(partida1);

            // When
            Partida result = partidaService.getPartidaByName("Partida1");

            // Then
            assertNotNull(result);
            assertEquals(partida1.getName(), result.getName());
            verify(partidaRepository).findByName("Partida1");
        }

        @Test
        void testGetPartidaByName_NotFound() {
            // Given
            when(partidaRepository.findByName("NonExistent")).thenReturn(null);

            // When
            Partida result = partidaService.getPartidaByName("NonExistent");

            // Then
            assertNull(result);
            verify(partidaRepository).findByName("NonExistent");
        }

        @Test
        void testGetSomePartidasByName_Success() {
            // Given
            List<String> names = Arrays.asList("Partida1", "Partida2");
            when(partidaRepository.findByName("Partida1")).thenReturn(partida1);
            when(partidaRepository.findByName("Partida2")).thenReturn(partida2);

            // When
            List<Partida> result = partidaService.getSomePartidasByName(names);

            // Then
            assertEquals(2, result.size());
            assertEquals("Partida1", result.get(0).getName());
            assertEquals("Partida2", result.get(1).getName());
        }
    }

    @Nested
    @DisplayName("POST Operations Tests")
    class PostOperationsTests {
        
        @Test
        void testSavePartida_Success() {
            // Given
            when(partidaRepository.save(any(Partida.class))).thenReturn(partida1);

            // When
            Partida result = partidaService.savePartida(partida1);

            // Then
            assertNotNull(result);
            assertEquals(partida1.getName(), result.getName());
            verify(partidaRepository).save(partida1);
        }

        @Test
        void testCopyPartida_Success() {
            // When
            Partida result = partidaService.copyPartida(partida1);

            // Then
            assertNotNull(result);
            assertEquals(partida1.getName(), result.getName());
            assertNull(result.getId()); // ID should not be copied
        }
    }

    @Nested
    @DisplayName("PUT Operations Tests")
    class PutOperationsTests {
        
        @Test
        void testUpdatePartidaById_Success() {
            // Given
            Partida updatedPartida = new Partida();
            updatedPartida.setName("UpdatedPartida");
            when(partidaRepository.findById(1)).thenReturn(Optional.of(partida1));
            when(partidaRepository.save(any(Partida.class))).thenReturn(updatedPartida);

            // When
            Partida result = partidaService.updatePartidaById(updatedPartida, 1);

            // Then
            assertNotNull(result);
            assertEquals(updatedPartida.getName(), result.getName());
            verify(partidaRepository).save(any(Partida.class));
        }

        @Test
        void testUpdatePartidaById_NotFound() {
            // Given
            when(partidaRepository.findById(99)).thenReturn(Optional.empty());

            // When/Then
            assertNull(partidaService.updatePartidaById(partida1, 99));
            verify(partidaRepository).findById(99);
        }
    }

    @Nested
    @DisplayName("DELETE Operations Tests")
    class DeleteOperationsTests {
        
        @Test
        void testDeleteAllPartidas() {
            // When
            partidaService.deleteAllPartidas();

            // Then
            verify(partidaRepository).deleteAll();
        }

        @Test
        void testDeletePartidaById_Success() {
            // Given
            when(partidaRepository.findById(1)).thenReturn(Optional.of(partida1));

            // When
            partidaService.deletePartidaById(1);

            // Then
            verify(partidaRepository).deleteById(1);
        }

        @Test
        void testDeleteSomePartidasById_Success() {
            // Given
            List<Integer> ids = Arrays.asList(1, 2);
            when(partidaRepository.findById(1)).thenReturn(Optional.of(partida1));
            when(partidaRepository.findById(2)).thenReturn(Optional.of(partida2));

            // When
            partidaService.deleteSomePartidasById(ids);

            // Then
            verify(partidaRepository).deleteById(1);
            verify(partidaRepository).deleteById(2);
        }
    }
}