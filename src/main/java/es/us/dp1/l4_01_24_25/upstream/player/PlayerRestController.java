package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.util.RestPreconditions;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/players")
public class PlayerRestController {
    
    private final PlayerService playerService;

    public PlayerRestController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<List<Player>> findAllJugadors() {
        return new ResponseEntity<>(playerService.getJugadores(), HttpStatus.OK);
    }

    @GetMapping("/names/{names}")
    public ResponseEntity<List<Player>> findSomeJugadorsByName(@PathVariable("names") List<String> names) throws ResourceNotFoundException {
        return new ResponseEntity<>(playerService.getSomeJugadoresByName(names), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> findJugadorById(@PathVariable("id")  Integer id) throws ResourceNotFoundException {
        Player p = playerService.getJugadorById(id);
        if (p == null)
            return new ResponseEntity<>(p, HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<>(p, HttpStatus.OK);
    }

    @GetMapping("/match/{id}")
    public ResponseEntity<List<Player>> findJugadoresByPartidaId(@PathVariable("id")  Integer id) throws ResourceNotFoundException {
        return new ResponseEntity<>(playerService.getPlayersByMatch(id), HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Player> findJugadorByName(@PathVariable("name")  String name) throws ResourceNotFoundException {
        Player p = playerService.getJugadorByName(name);
        if (p == null)
            return new ResponseEntity<>(p, HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<>(p, HttpStatus.OK);
    }


    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteAllJugadors() {
        playerService.deleteAllJugadores();
    }

    @DeleteMapping(value = "/ids/{ids}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Object> deleteSomeById(@PathVariable("ids") List<Integer> ids) throws ErrorMessage {
		List<Integer> idsJugadorsNoBorradas = new LinkedList<>();
        Integer numJugadorsEncontradas = 0;
        for (Integer id : ids) {
            RestPreconditions.checkNotNull(playerService.getJugadorById(id), "Jugador", "ID", id);
		if (playerService.getJugadorById(id) != null) {
            numJugadorsEncontradas++;
		} else
            idsJugadorsNoBorradas.add(id);
        }
        if (numJugadorsEncontradas == ids.size()) {
            playerService.deleteSomeById(ids);
            return new ResponseEntity<>(new MessageResponse("Jugadors borradas"), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(idsJugadorsNoBorradas, HttpStatus.NOT_FOUND);
            // new ErrorMessage(422, new Date(), "La Jugador no pudo ser borrada", "Probablemente no existiese")
        }
        
	}

    @DeleteMapping(value = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Object> deleteById(@PathVariable("id") Integer id) throws ErrorMessage {
		RestPreconditions.checkNotNull(playerService.getJugadorById(id), "Jugador", "ID", id);
		if (playerService.getJugadorById(id) != null) {
			playerService.deleteJugadorById(id);
			return new ResponseEntity<>(new MessageResponse("Jugador borrada"), HttpStatus.OK);
		} else
			return new ResponseEntity<>(new ErrorMessage(422, new Date(), 
            "La Jugador no pudo ser borrada", "Probablemente no existiese"), HttpStatus.NOT_FOUND) ;
	}


    @PutMapping("/{id}")
    public ResponseEntity<Player> updateJugadorById(@PathVariable("id") Integer idToUpdate, 
    @RequestBody @Valid Player jugadorNueva) {
        RestPreconditions.checkNotNull(playerService.getJugadorById(idToUpdate), "Jugador", "ID", idToUpdate);
        return new ResponseEntity<>(playerService.updateJugadorById(jugadorNueva,idToUpdate), HttpStatus.OK);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Player> createJugador(@RequestBody @Valid Player jugador) throws DataAccessException{
        return new ResponseEntity<>(playerService.saveJugador(jugador), HttpStatus.OK);
    }

    @GetMapping("/{id}/salmonMatch")
    public ResponseEntity<List<SalmonMatch>> findSalmonMatchFromPlayer(Integer playerId) {  
        return new ResponseEntity<>(playerService.getSalmonsByPlayerId(playerId), HttpStatus.OK);
    }

    @PatchMapping("/{id}/energy")
    public ResponseEntity<Player> updateRound(@PathVariable("id") Integer id, @RequestBody @Valid Integer energyUsed) throws ResourceNotFoundException {
        Player jugador = playerService.getById(id);
        //System.out.println(jugador);
        if (jugador == null) {
            throw new ResourceNotFoundException("Partida no encontrada", "id", id.toString());
        }
        jugador.setEnergy(jugador.getEnergy() - energyUsed);
        return new ResponseEntity<>(jugador, HttpStatus.OK);
    }
    
}