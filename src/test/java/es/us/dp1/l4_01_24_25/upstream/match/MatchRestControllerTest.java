package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.us.dp1.l4_01_24_25.upstream.auth.payload.response.MessageResponse;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ErrorMessage;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;

class matchRestControllerTest {

    @Mock
    private MatchService matchService;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private MatchRestController partidaRestController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAllPartidas_Positive() {
        List<Match> partidas = Arrays.asList(new Match(), new Match());
        when(matchService.getPartidas()).thenReturn(partidas);
        
        ResponseEntity<List<Match>> response = partidaRestController.findAllPartidas();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(partidas, response.getBody());
    }

    @Test
    void testFindAllPartidas_Negative() {
        when(matchService.getPartidas()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Match>> response = partidaRestController.findAllPartidas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testFindSomePartidasByName_Positive() throws ResourceNotFoundException {
        List<String> nombres = Arrays.asList("Partida1", "Partida2");
        List<Match> partidas = Arrays.asList(new Match(), new Match());
        when(matchService.getSomePartidasByName(nombres)).thenReturn(partidas);

        ResponseEntity<List<Match>> response = partidaRestController.findSomePartidasByName(nombres);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(partidas, response.getBody());
    }

    @Test
    void testFindSomePartidasByName_Negative() throws ResourceNotFoundException {
        List<String> nombres = Arrays.asList("Partida1", "NoExiste");
        when(matchService.getSomePartidasByName(nombres))
                .thenThrow(new ResourceNotFoundException("Una o más partidas no encontradas"));

        assertThrows(ResourceNotFoundException.class, () -> {
            partidaRestController.findSomePartidasByName(nombres);
        });
    }

    @Test
    void testFindPartidaById_Positive() throws ResourceNotFoundException {
        Match partida = new Match();
        when(matchService.getPartidaById(1)).thenReturn(partida);

        ResponseEntity<Match> response = partidaRestController.findPartidaById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(partida, response.getBody());
    }

    @Test
    void testFindPartidaById_Negative() throws ResourceNotFoundException {
        when(matchService.getPartidaById(1)).thenReturn(null);

        ResponseEntity<Match> response = partidaRestController.findPartidaById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testFindPartidaByName_Positive() throws ResourceNotFoundException {
        Match partida = new Match();
        when(matchService.getPartidaByName("Partida1")).thenReturn(partida);

        ResponseEntity<Match> response = partidaRestController.findPartidaByName("Partida1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(partida, response.getBody());
    }

    @Test
    void testFindPartidaByName_Negative() {
        // Simulamos que al buscar la partida por nombre, se lanza una ResourceNotFoundException
        when(matchService.getPartidaByName("NonExistent")).thenThrow(new ResourceNotFoundException("Partida no encontrada"));

        assertThrows(ResourceNotFoundException.class, () -> {
            partidaRestController.findPartidaByName("NonExistent");
        });
    }

    @Test
    void testFindPlayersFromGame_Positive() throws ResourceNotFoundException {
        List<Player> jugadores = Arrays.asList(new Player(), new Player());
        when(matchService.getPlayersFromGame(1)).thenReturn(jugadores);

        ResponseEntity<List<Player>> response = partidaRestController.findPlayersFromGame(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(jugadores, response.getBody());
    }

    @Test
    void testFindPlayersFromGame_Negative() throws ResourceNotFoundException {
        when(matchService.getPlayersFromGame(1)).thenReturn(null);

        ResponseEntity<List<Player>> response = partidaRestController.findPlayersFromGame(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreatePartida_Positive() {
        Match partida = new Match();
        when(matchService.savePartida(partida)).thenReturn(partida);

        ResponseEntity<Match> response = partidaRestController.createPartida(partida);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(partida, response.getBody());
    }

    @Test
    void testCreatePartida_Negative() {
        Match partidaInvalida = null;  // Asume que los campos necesarios no están inicializados
        //when(partidaService.savePartida(partidaInvalida)).thenThrow(new IllegalArgumentException("Datos inválidos"));

        ResponseEntity<Match> response = partidaRestController.createPartida(partidaInvalida);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeletePartidaById_Positive() throws ErrorMessage {
        Match partida = new Match();
        when(matchService.getPartidaById(1)).thenReturn(partida);

        ResponseEntity<Object> response = partidaRestController.deletePartidaById(1);

        verify(matchService, times(1)).deletePartidaById(1);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeletePartidaById_Negative() {
    // Simulamos que al buscar la partida con id 1, se lanza una ResourceNotFoundException
    doThrow(new ResourceNotFoundException("Partida no encontrada")).when(matchService).getPartidaById(1);

    // Verificamos que el controlador lanza la excepción esperada
    assertThrows(ResourceNotFoundException.class, () -> {
        partidaRestController.deletePartidaById(1);
        });
    }

    @Test
    void testDeleteSomePartidasById_Positive() throws ErrorMessage {
        // IDs de las partidas a borrar
        List<Integer> ids = Arrays.asList(1, 2, 3);

        // Simulamos que todas las partidas existen (getPartidaById devuelve una partida no nula)
        when(matchService.getPartidaById(1)).thenReturn(new Match());
        when(matchService.getPartidaById(2)).thenReturn(new Match());
        when(matchService.getPartidaById(3)).thenReturn(new Match());

        // Simulamos que la eliminación no lanza excepciones (doNothing porque es void). INNECESARIO
        doNothing().when(matchService).deleteSomePartidasById(ids);

        // Llamada al controlador
        ResponseEntity<Object> response = partidaRestController.deleteSomePartidasById(ids);

        // Verificamos que el servicio deleteSomePartidasById se haya llamado con los IDs correctos. INNECESARIO
        verify(matchService, times(1)).deleteSomePartidasById(ids);
        
        // Verificamos que la respuesta es correcta, debería ser OK ya que todas las partidas se borraron
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Partidas borradas", ((MessageResponse) response.getBody()).getMessage());
    }

    @Test
    void testDeleteSomePartidasById_Negative() throws ErrorMessage {
        List<Integer> ids = Arrays.asList(1, 99); // Suponemos que la partida con id 99 no existe
        doThrow(new ResourceNotFoundException("Una o más partidas no encontradas")).when(matchService).deleteSomePartidasById(ids);

        assertThrows(ResourceNotFoundException.class, () -> {
            partidaRestController.deleteSomePartidasById(ids);
        });
    }

    @Test
    void testUpdatePartidaById_Positive() throws ErrorMessage {
        Match partida = new Match();
        when(matchService.getPartidaById(1)).thenReturn(partida);
        when(matchService.updatePartidaById(partida, 1)).thenReturn(partida);

        ResponseEntity<Match> response = partidaRestController.updatePartidaById(1, partida);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(partida, response.getBody());
    }

    @Test
    void testUpdatePartidaById_Negative() {
        // Simulamos que al buscar la partida con id 1, se lanza una ResourceNotFoundException
        doThrow(new ResourceNotFoundException("Partida no encontrada")).when(matchService).getPartidaById(1);
    
        // Verificamos que el controlador lanza la excepción esperada
        assertThrows(ResourceNotFoundException.class, () -> {
            partidaRestController.updatePartidaById(1, new Match());
        });
    }
}