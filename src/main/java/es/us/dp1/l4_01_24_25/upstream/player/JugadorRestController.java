package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.auth.payload.response.MessageResponse;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ErrorMessage;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.util.RestPreconditions;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/players")
public class JugadorRestController {
    
    private final JugadorService jugadorService;

    public JugadorRestController(JugadorService jugadorService) {
        this.jugadorService = jugadorService;
    }

    @GetMapping
    public ResponseEntity<List<Jugador>> findAllJugadors() {
        return new ResponseEntity<>(jugadorService.getJugadores(), HttpStatus.OK);
    }

    @GetMapping("/names/{names}")
    public ResponseEntity<List<Jugador>> findSomeJugadorsByName(@PathVariable("names") List<String> names) throws ResourceNotFoundException {
        return new ResponseEntity<>(jugadorService.getSomeJugadoresByName(names), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Jugador> findJugadorById(@PathVariable("id")  Integer id) throws ResourceNotFoundException {
        Jugador p = jugadorService.getJugadorById(id);
        if (p == null)
            return new ResponseEntity<>(p, HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<>(p, HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Jugador> findJugadorByName(@PathVariable("name")  String name) throws ResourceNotFoundException {
        Jugador p = jugadorService.getJugadorByName(name);
        if (p == null)
            return new ResponseEntity<>(p, HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<>(p, HttpStatus.OK);
    }


    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteAllJugadors() {
        jugadorService.deleteAllJugadores();
    }

    @DeleteMapping(value = "/ids/{ids}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Object> deleteSomeJugadorsById(@PathVariable("ids") List<Integer> ids) throws ErrorMessage {
		List<Integer> idsJugadorsNoBorradas = new LinkedList<>();
        Integer numJugadorsEncontradas = 0;
        for (Integer id : ids) {
            RestPreconditions.checkNotNull(jugadorService.getJugadorById(id), "Jugador", "ID", id);
		if (jugadorService.getJugadorById(id) != null) {
            numJugadorsEncontradas++;
		} else
            idsJugadorsNoBorradas.add(id);
        }
        if (numJugadorsEncontradas == ids.size()) {
            jugadorService.deleteSomeJugadoresById(ids);
            return new ResponseEntity<>(new MessageResponse("Jugadors borradas"), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(idsJugadorsNoBorradas, HttpStatus.NOT_FOUND);
            // new ErrorMessage(422, new Date(), "La Jugador no pudo ser borrada", "Probablemente no existiese")
        }
        
	}

    @DeleteMapping(value = "{id}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Object> deleteJugadorById(@PathVariable("id") Integer id) throws ErrorMessage {
		RestPreconditions.checkNotNull(jugadorService.getJugadorById(id), "Jugador", "ID", id);
		if (jugadorService.getJugadorById(id) != null) {
			jugadorService.deleteJugadorById(id);
			return new ResponseEntity<>(new MessageResponse("Jugador borrada"), HttpStatus.OK);
		} else
			return new ResponseEntity<>(new ErrorMessage(422, new Date(), 
            "La Jugador no pudo ser borrada", "Probablemente no existiese"), HttpStatus.NOT_FOUND) ;
	}


    @PutMapping("/{id}")
    public ResponseEntity<Jugador> updateJugadorById(@PathVariable("id") Integer idToUpdate, 
    @RequestBody @Valid Jugador jugadorNueva) {
        RestPreconditions.checkNotNull(jugadorService.getJugadorById(idToUpdate), "Jugador", "ID", idToUpdate);
        return new ResponseEntity<>(jugadorService.updateJugadorById(jugadorNueva,idToUpdate), HttpStatus.OK);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Jugador> createJugador(@RequestBody @Valid Jugador jugador) throws DataAccessException{
        return new ResponseEntity<>(jugadorService.saveJugador(jugador), HttpStatus.OK);
    }

    // Copy jugador

}