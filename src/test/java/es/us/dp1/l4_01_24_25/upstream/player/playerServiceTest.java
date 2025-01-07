package es.us.dp1.l4_01_24_25.upstream.player;

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

@ExtendWith(MockitoExtension.class)
class playerServiceTest {

    @Mock
    private PlayerRepository jugadorRepository;

    @InjectMocks
    private PlayerService jugadorService;

    private Player jugador1;
    private Player jugador2;

    @BeforeEach
    void setup() {
        jugador1 = new Player();
        jugador1.setId(1);
        jugador1.setName("Jugador1");

        jugador2 = new Player();
        jugador2.setId(2);
        jugador2.setName("Jugador2");
    }

    @Test
    void testGetJugadores() {
        List<Player> jugadores = Arrays.asList(jugador1, jugador2);
        when(jugadorRepository.findAll()).thenReturn(jugadores);

        List<Player> result = jugadorService.getJugadores();

        assertEquals(2, result.size());
        verify(jugadorRepository).findAll();
    }

    @Test
    void testGetSomeJugadoresById_Success() {
        when(jugadorRepository.findById(1)).thenReturn(Optional.of(jugador1));
        when(jugadorRepository.findById(2)).thenReturn(Optional.of(jugador2));

        List<Integer> ids = Arrays.asList(1, 2);
        List<Player> result = jugadorService.getSomeById(ids);

        assertEquals(2, result.size());
        assertEquals("Jugador1", result.get(0).getName());
        assertEquals("Jugador2", result.get(1).getName());
    }

    @Test
    void testGetSomeJugadoresById_NotFound() {
        when(jugadorRepository.findById(any())).thenReturn(Optional.empty());

        List<Integer> ids = Arrays.asList(1, 2);
        assertThrows(ResourceNotFoundException.class, () -> 
            jugadorService.getSomeById(ids));
    }

    @Test
    void testGetSomeJugadoresByName_Success() {
        when(jugadorRepository.findByName("Jugador1")).thenReturn(jugador1);
        when(jugadorRepository.findByName("Jugador2")).thenReturn(jugador2);

        List<String> names = Arrays.asList("Jugador1", "Jugador2");
        List<Player> result = jugadorService.getSomeJugadoresByName(names);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
    }

    @Test
    void testGetSomeJugadoresByName_NotFound() {
        when(jugadorRepository.findByName(any())).thenReturn(null);

        List<String> names = Arrays.asList("Jugador1", "Jugador2");
        assertThrows(ResourceNotFoundException.class, () -> 
            jugadorService.getSomeJugadoresByName(names));
    }

    @Test
    void testGetJugadorById_Success() {
        when(jugadorRepository.findById(1)).thenReturn(Optional.of(jugador1));

        Player result = jugadorService.getById(1);

        assertEquals("Jugador1", result.getName());
        verify(jugadorRepository).findById(1);
    }

    @Test
    void testGetJugadorById_NotFound() {
        when(jugadorRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            jugadorService.getById(1));
    }

    @Test
    void testGetJugadorByName_Success() {
        when(jugadorRepository.findByName("Jugador1")).thenReturn(jugador1);

        Player result = jugadorService.getByName("Jugador1");

        assertEquals(1, result.getId());
        verify(jugadorRepository).findByName("Jugador1");
    }

    @Test
    void testGetJugadorByName_NotFound() {
        when(jugadorRepository.findByName(any())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> 
            jugadorService.getByName("NonExistent"));
    }

    @Test
    void testDeleteAllJugadores() {
        jugadorService.deleteAllJugadores();
        verify(jugadorRepository).deleteAll();
    }

    @Test
    void testDeleteSomeJugadoresById_Success() {
        when(jugadorRepository.findById(1)).thenReturn(Optional.of(jugador1));
        when(jugadorRepository.findById(2)).thenReturn(Optional.of(jugador2));

        List<Integer> ids = Arrays.asList(1, 2);
        jugadorService.deleteSomeById(ids);

        verify(jugadorRepository, times(2)).deleteById(any());
    }

    @Test
    void testDeleteSomeJugadoresById_NotFound() {
        when(jugadorRepository.findById(any())).thenReturn(Optional.empty());

        List<Integer> ids = Arrays.asList(1, 2);
        assertThrows(ResourceNotFoundException.class, () -> 
            jugadorService.deleteSomeById(ids));
    }

    @Test
    void testDeleteJugadorById_Success() {
        when(jugadorRepository.findById(1)).thenReturn(Optional.of(jugador1));

        jugadorService.deleteJugadorById(1);

        verify(jugadorRepository).deleteById(1);
    }

    @Test
    void testDeleteJugadorById_NotFound() {
        when(jugadorRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            jugadorService.deleteJugadorById(1));
    }

    @Test
    void testUpdateJugadorById_Success() {
        Player updatedJugador = new Player();
        updatedJugador.setId(1);
        updatedJugador.setName("UpdatedJugador");

        when(jugadorRepository.findById(1)).thenReturn(Optional.of(jugador1));
        when(jugadorRepository.save(any())).thenReturn(updatedJugador);

        Player result = jugadorService.updateJugadorById(updatedJugador, 1);

        assertEquals("UpdatedJugador", result.getName());
    }

    @Test
    void testUpdateJugadorById_NotFound() {
        when(jugadorRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            jugadorService.updateJugadorById(new Player(), 1));
    }

    @Test
    void testSaveJugador_Success() {
        when(jugadorRepository.save(any(Player.class))).thenReturn(jugador1);

        Player result = jugadorService.savePlayer(jugador1);

        assertEquals("Jugador1", result.getName());
        verify(jugadorRepository).save(jugador1);
    }

    @Test
    void testSaveJugador_Error() {
        when(jugadorRepository.save(any(Player.class))).thenThrow(new DataIntegrityViolationException("Error en la base de datos"));

        assertThrows(DataAccessException.class, () -> 
            jugadorService.savePlayer(jugador1));
    }

    @Test
    void testSaveJugadores_Success() {
        List<Player> Jugadores = Arrays.asList(jugador1, jugador2);
        when(jugadorRepository.save(any(Player.class))).thenReturn(jugador1, jugador2);

        List<Player> result = jugadorService.saveJugadores(Jugadores);

        assertTrue(result.isEmpty());
        verify(jugadorRepository, times(2)).save(any(Player.class));
    }

    @Test
    void testSaveJugadores_PartialSuccess() {
        List<Player> Jugadores = Arrays.asList(jugador1, jugador2);
        when(jugadorRepository.save(jugador1)).thenReturn(jugador1);
        when(jugadorRepository.save(jugador2)).thenThrow(new DataAccessException("Error") {});

        List<Player> result = jugadorService.saveJugadores(Jugadores);

        assertEquals(1, result.size());
        assertTrue(result.contains(jugador2));
    }


    @Test
    void testDeleteSomeJugadoresByName_Success() {
        when(jugadorRepository.findByName("Jugador1")).thenReturn(jugador1);
        when(jugadorRepository.findByName("Jugador2")).thenReturn(jugador2);

        List<String> names = Arrays.asList("Jugador1", "Jugador2");
        jugadorService.deleteSomeJugadoresByName(names);

        verify(jugadorRepository, times(2)).delete(any());
    }

    @Test
    void testDeleteSomeJugadoresByName_NotFound() {
        when(jugadorRepository.findByName(any())).thenReturn(null);

        List<String> names = Arrays.asList("Jugador1", "Jugador2");
        assertThrows(ResourceNotFoundException.class, () -> 
            jugadorService.deleteSomeJugadoresByName(names));
    }

    @Test
    void testDeleteSomeJugadoresByName_PartialNotFound() {
        when(jugadorRepository.findByName("Jugador1")).thenReturn(jugador1);
        when(jugadorRepository.findByName("Jugador2")).thenReturn(null);

        List<String> names = Arrays.asList("Jugador1", "Jugador2");
        assertThrows(ResourceNotFoundException.class, () -> 
            jugadorService.deleteSomeJugadoresByName(names));
    }

    // Additional tests for updateJugadorByName
    @Test
    void testUpdateJugadorByName_Success() {
        
        // Creamos la Jugador original que está en la base de datos
        Player jugador1 = new Player();
        jugador1.setName("Jugador1");
        jugador1.setColor(Color.AMARILLO);
        jugador1.setPlayerOrder(2);
        jugador1.setAlive(false);
        jugador1.setPoints(10);

        // Creamos la Jugador actualizada que queremos guardar
        Player updatedJugador = new Player();
        updatedJugador.setName("UpdatedJugador");
        updatedJugador.setColor(Color.AMARILLO);
        updatedJugador.setPlayerOrder(2);
        updatedJugador.setAlive(false);
        updatedJugador.setPoints(10);

        // Simulamos el comportamiento del repositorio
        when(jugadorRepository.findByName("Jugador1")).thenReturn(jugador1);
        when(jugadorRepository.save(any())).thenReturn(updatedJugador);

        // Llamamos al método de servicio que queremos probar
        Player result = jugadorService.updateJugadorByName(updatedJugador, "Jugador1");

        // Verificamos que la Jugador devuelta sea la actualizada
        assertEquals("UpdatedJugador", result.getName());

    }

    @Test
    void testUpdateJugadorByName_NotFound() {
        when(jugadorRepository.findByName(any())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> 
            jugadorService.updateJugadorByName(new Player(), "NonExistent"));
    }

    // Additional edge cases for existing methods
    @Test
    void testGetSomeJugadoresById_EmptyList() {
        List<Integer> ids = Arrays.asList();
        List<Player> result = jugadorService.getSomeById(ids);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSomeJugadoresByName_EmptyList() {
        List<String> names = Arrays.asList();
        List<Player> result = jugadorService.getSomeJugadoresByName(names);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveJugadores_EmptyList() {
        List<Player> jugadores = Arrays.asList();
        List<Player> result = jugadorService.saveJugadores(jugadores);
        assertTrue(result.isEmpty());
    }
}