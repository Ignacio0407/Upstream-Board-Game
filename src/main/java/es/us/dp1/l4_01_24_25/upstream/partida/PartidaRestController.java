package es.us.dp1.l4_01_24_25.upstream.partida;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("api/v1/matches")
public class PartidaRestController {
    
    private final PartidaService partidaService;

    public PartidaRestController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    @GetMapping
    public ResponseEntity<List<Partida>> findAllPartidas() {
        return new ResponseEntity<>(partidaService.getPartidas(), HttpStatus.OK);
    }

    // TODO
    @GetMapping("/user/{id}")
    public ResponseEntity<List<Partida>> findPartidasFromUser() {
        return null;
    }

    @GetMapping("/names/{names}")
    public ResponseEntity<List<Partida>> findSomePartidasByName(@PathVariable("names") List<String> names) throws ResourceNotFoundException {
        return new ResponseEntity<>(partidaService.getSomePartidasByName(names), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Partida> findPartidaById(@PathVariable("id")  Integer id) throws ResourceNotFoundException {
        Partida p = partidaService.getPartidaById(id);
        if (p == null)
            return new ResponseEntity<>(p, HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<>(p, HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Partida> findPartidaByName(@PathVariable("name")  String name) throws ResourceNotFoundException {
        Partida p = partidaService.getPartidaByName(name);
        if (p == null)
            return new ResponseEntity<>(p, HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<>(p, HttpStatus.OK);
    }


    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("ADMIN")
    public void deleteAllPartidas() {
        partidaService.deleteAllPartidas();
    }

    @DeleteMapping(value = "/ids/{ids}")
	@ResponseStatus(HttpStatus.OK)
    @PreAuthorize("ADMIN")
	public ResponseEntity<Object> deleteSomePartidasById(@PathVariable("ids") List<Integer> ids) throws ErrorMessage {
		List<Integer> idsPartidasNoBorradas = new LinkedList<>();
        Integer numPartidasEncontradas = 0;
        for (Integer id : ids) {
            RestPreconditions.checkNotNull(partidaService.getPartidaById(id), "Partida", "ID", id);
		if (partidaService.getPartidaById(id) != null) {
            numPartidasEncontradas++;
		} else
            idsPartidasNoBorradas.add(id);
        }
        if (numPartidasEncontradas == ids.size()) {
            partidaService.deleteSomePartidasById(ids);
            return new ResponseEntity<>(new MessageResponse("Partidas borradas"), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(idsPartidasNoBorradas, HttpStatus.NOT_FOUND);
            // new ErrorMessage(422, new Date(), "La Partida no pudo ser borrada", "Probablemente no existiese")
        }
        
	}

    @DeleteMapping(value = "{id}")
	@ResponseStatus(HttpStatus.OK)
    @PreAuthorize("ADMIN")
	public ResponseEntity<Object> deletePartidaById(@PathVariable("id") Integer id) throws ErrorMessage {
		Partida p = partidaService.getPartidaById(id);
        RestPreconditions.checkNotNull(p, "Partida", "id", id);
		if (p != null) {
			partidaService.deletePartidaById(id);
			return new ResponseEntity<>(new MessageResponse("Partida borrada"), HttpStatus.NO_CONTENT);
		} else
			return new ResponseEntity<>(new ErrorMessage(422, new Date(), 
            "La Partida no pudo ser borrada", "Probablemente no existiese"), HttpStatus.NOT_FOUND) ;
	}


    @PutMapping("/{id}")
    public ResponseEntity<Partida> updatePartidaById(@PathVariable("id") Integer idToUpdate, 
    @RequestBody @Valid Partida partidaNueva) {
        RestPreconditions.checkNotNull(partidaService.getPartidaById(idToUpdate), "Partida", "ID", idToUpdate);
        return new ResponseEntity<>(partidaService.updatePartidaById(partidaNueva,idToUpdate), HttpStatus.OK);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Partida> createPartida(@RequestBody @Valid Partida partida) throws DataAccessException{
        System.out.println(partida);
        return new ResponseEntity<>(partidaService.savePartida(partida), HttpStatus.OK);
    }

}