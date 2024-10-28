package es.us.dp1.l4_01_24_25.upstream.partida;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.player.Jugador;

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

    @Test
    void testGetPartidas() {
        List<Partida> partidas = Arrays.asList(partida1, partida2);
        when(partidaRepository.findAll()).thenReturn(partidas);

        List<Partida> result = partidaService.getPartidas();

        assertEquals(2, result.size());
        verify(partidaRepository).findAll();
    }

    @Test
    void testGetSomePartidasById_Success() {
        when(partidaRepository.findById(1)).thenReturn(Optional.of(partida1));
        when(partidaRepository.findById(2)).thenReturn(Optional.of(partida2));

        List<Integer> ids = Arrays.asList(1, 2);
        List<Partida> result = partidaService.getSomePartidasById(ids);

        assertEquals(2, result.size());
        assertEquals("Partida1", result.get(0).getName());
        assertEquals("Partida2", result.get(1).getName());
    }

    @Test
    void testGetSomePartidasById_NotFound() {
        when(partidaRepository.findById(any())).thenReturn(Optional.empty());

        List<Integer> ids = Arrays.asList(1, 2);
        assertThrows(ResourceNotFoundException.class, () -> 
            partidaService.getSomePartidasById(ids));
    }

    @Test
    void testGetSomePartidasByName_Success() {
        when(partidaRepository.findByName("Partida1")).thenReturn(partida1);
        when(partidaRepository.findByName("Partida2")).thenReturn(partida2);

        List<String> names = Arrays.asList("Partida1", "Partida2");
        List<Partida> result = partidaService.getSomePartidasByName(names);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
    }

    @Test
    void testGetSomePartidasByName_NotFound() {
        when(partidaRepository.findByName(any())).thenReturn(null);

        List<String> names = Arrays.asList("Partida1", "Partida2");
        assertThrows(ResourceNotFoundException.class, () -> 
            partidaService.getSomePartidasByName(names));
    }

    @Test
    void testGetPartidaById_Success() {
        when(partidaRepository.findById(1)).thenReturn(Optional.of(partida1));

        Partida result = partidaService.getPartidaById(1);

        assertEquals("Partida1", result.getName());
        verify(partidaRepository).findById(1);
    }

    @Test
    void testGetPartidaById_NotFound() {
        when(partidaRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            partidaService.getPartidaById(1));
    }

    @Test
    void testGetPartidaByName_Success() {
        when(partidaRepository.findByName("Partida1")).thenReturn(partida1);

        Partida result = partidaService.getPartidaByName("Partida1");

        assertEquals(1, result.getId());
        verify(partidaRepository).findByName("Partida1");
    }

    @Test
    void testGetPartidaByName_NotFound() {
        when(partidaRepository.findByName(any())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> 
            partidaService.getPartidaByName("NonExistent"));
    }

    @Test
    void testDeleteAllPartidas() {
        partidaService.deleteAllPartidas();
        verify(partidaRepository).deleteAll();
    }

    @Test
    void testDeleteSomePartidasById_Success() {
        when(partidaRepository.findById(1)).thenReturn(Optional.of(partida1));
        when(partidaRepository.findById(2)).thenReturn(Optional.of(partida2));

        List<Integer> ids = Arrays.asList(1, 2);
        partidaService.deleteSomePartidasById(ids);

        verify(partidaRepository, times(2)).deleteById(any());
    }

    @Test
    void testDeleteSomePartidasById_NotFound() {
        when(partidaRepository.findById(any())).thenReturn(Optional.empty());

        List<Integer> ids = Arrays.asList(1, 2);
        assertThrows(ResourceNotFoundException.class, () -> 
            partidaService.deleteSomePartidasById(ids));
    }

    @Test
    void testDeletePartidaById_Success() {
        when(partidaRepository.findById(1)).thenReturn(Optional.of(partida1));

        partidaService.deletePartidaById(1);

        verify(partidaRepository).deleteById(1);
    }

    @Test
    void testDeletePartidaById_NotFound() {
        when(partidaRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            partidaService.deletePartidaById(1));
    }

    @Test
    void testUpdatePartidaById_Success() {
        Partida updatedPartida = new Partida();
        updatedPartida.setId(1);
        updatedPartida.setName("UpdatedPartida");

        when(partidaRepository.findById(1)).thenReturn(Optional.of(partida1));
        when(partidaRepository.save(any())).thenReturn(updatedPartida);

        Partida result = partidaService.updatePartidaById(updatedPartida, 1);

        assertEquals("UpdatedPartida", result.getName());
    }

    @Test
    void testUpdatePartidaById_NotFound() {
        when(partidaRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            partidaService.updatePartidaById(new Partida(), 1));
    }

    @Test
    void testSavePartida_Success() {
        when(partidaRepository.save(any(Partida.class))).thenReturn(partida1);

        Partida result = partidaService.savePartida(partida1);

        assertEquals("Partida1", result.getName());
        verify(partidaRepository).save(partida1);
    }

    @Test
    void testSavePartida_Error() {
        when(partidaRepository.save(any(Partida.class))).thenThrow(new DataIntegrityViolationException("Error en la base de datos"));

        assertThrows(DataAccessException.class, () -> 
            partidaService.savePartida(partida1));
    }

    @Test
    void testSavePartidas_Success() {
        List<Partida> partidas = Arrays.asList(partida1, partida2);
        when(partidaRepository.save(any(Partida.class))).thenReturn(partida1, partida2);

        List<Partida> result = partidaService.savePartidas(partidas);

        assertTrue(result.isEmpty());
        verify(partidaRepository, times(2)).save(any(Partida.class));
    }

    @Test
    void testSavePartidas_PartialSuccess() {
        List<Partida> partidas = Arrays.asList(partida1, partida2);
        when(partidaRepository.save(partida1)).thenReturn(partida1);
        when(partidaRepository.save(partida2)).thenThrow(new DataAccessException("Error") {});

        List<Partida> result = partidaService.savePartidas(partidas);

        assertEquals(1, result.size());
        assertTrue(result.contains(partida2));
    }


    @Test
    void testDeleteSomePartidasByName_Success() {
        when(partidaRepository.findByName("Partida1")).thenReturn(partida1);
        when(partidaRepository.findByName("Partida2")).thenReturn(partida2);

        List<String> names = Arrays.asList("Partida1", "Partida2");
        partidaService.deleteSomePartidasByName(names);

        verify(partidaRepository, times(2)).delete(any());
    }

    @Test
    void testDeleteSomePartidasByName_NotFound() {
        when(partidaRepository.findByName(any())).thenReturn(null);

        List<String> names = Arrays.asList("Partida1", "Partida2");
        assertThrows(ResourceNotFoundException.class, () -> 
            partidaService.deleteSomePartidasByName(names));
    }

    @Test
    void testDeleteSomePartidasByName_PartialNotFound() {
        when(partidaRepository.findByName("Partida1")).thenReturn(partida1);
        when(partidaRepository.findByName("Partida2")).thenReturn(null);

        List<String> names = Arrays.asList("Partida1", "Partida2");
        assertThrows(ResourceNotFoundException.class, () -> 
            partidaService.deleteSomePartidasByName(names));
    }

    // Additional tests for updatePartidaByName
    @Test
    void testUpdatePartidaByName_Success() {
        // Creamos un jugador inicial y un jugador actual de ejemplo
        Jugador jugadorInicial = new Jugador();
        Jugador jugadorActual = new Jugador();
        
        // Creamos la partida original que está en la base de datos
        Partida partida1 = new Partida();
        partida1.setName("Partida1");
        partida1.setContrasena("Pikachu");
        partida1.setEstado(Estado.EN_CURSO);
        partida1.setNumJugadores(2);
        partida1.setRonda(4);
        partida1.setFase(Fase.MOVIENDO);
        partida1.setJugadorInicial(jugadorInicial);
        partida1.setJugadorActual(jugadorActual);

        // Creamos la partida actualizada que queremos guardar
        Partida updatedPartida = new Partida();
        updatedPartida.setName("UpdatedPartida");
        updatedPartida.setContrasena("Pikachu");
        updatedPartida.setEstado(Estado.EN_CURSO);
        updatedPartida.setNumJugadores(2);
        updatedPartida.setRonda(5);
        updatedPartida.setFase(Fase.MOVIENDO);
        updatedPartida.setJugadorInicial(jugadorInicial);
        updatedPartida.setJugadorActual(jugadorActual);

        // Simulamos el comportamiento del repositorio
        when(partidaRepository.findByName("Partida1")).thenReturn(partida1);
        when(partidaRepository.save(any())).thenReturn(updatedPartida);

        // Llamamos al método de servicio que queremos probar
        Partida result = partidaService.updatePartidaByName(updatedPartida, "Partida1");

        // Verificamos que la partida devuelta sea la actualizada
        assertEquals("UpdatedPartida", result.getName());

    }

    @Test
    void testUpdatePartidaByName_NotFound() {
        when(partidaRepository.findByName(any())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> 
            partidaService.updatePartidaByName(new Partida(), "NonExistent"));
    }

    // Additional edge cases for existing methods
    @Test
    void testGetSomePartidasById_EmptyList() {
        List<Integer> ids = Arrays.asList();
        List<Partida> result = partidaService.getSomePartidasById(ids);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSomePartidasByName_EmptyList() {
        List<String> names = Arrays.asList();
        List<Partida> result = partidaService.getSomePartidasByName(names);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSavePartidas_EmptyList() {
        List<Partida> partidas = Arrays.asList();
        List<Partida> result = partidaService.savePartidas(partidas);
        assertTrue(result.isEmpty());
    }
}