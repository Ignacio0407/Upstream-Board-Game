package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.player.UserSerializer;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.salmonMatchService;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileType;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import es.us.dp1.l4_01_24_25.upstream.util.RestPreconditions;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/matches")
public class MatchRestController {
    
    private final MatchService matchService;
    private final PlayerService playerService;
    private final UserService userService;
    private final MatchTileService matchTileService;
    private final salmonMatchService sms;

    public MatchRestController(MatchService partidaService, PlayerService jugadorService, UserService userService, MatchTileService matchTileService, salmonMatchService sms) {
        this.matchService = partidaService;
        this.playerService = jugadorService;
        this.userService = userService;
        this.matchTileService = matchTileService;
        this.sms = sms;
    }

    @GetMapping
    public ResponseEntity<List<Match>> findAllPartidas() {
        return new ResponseEntity<>(matchService.getAll(), HttpStatus.OK);
    }

    // TODO
    @GetMapping("/user/{id}")
    public ResponseEntity<List<Match>> findPartidasFromUser() {
        return null;
    }

    @GetMapping("/names/{names}")
    public ResponseEntity<List<Match>> findSomeByName(@PathVariable("names") List<String> names) throws ResourceNotFoundException {
        return new ResponseEntity<>(matchService.getSomeByName(names), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> findById (@PathVariable("id")  Integer id) throws ResourceNotFoundException {
        Match p = matchService.getById(id);
        if (p == null)
            return new ResponseEntity<>(p, HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<>(p, HttpStatus.OK);
    }

    @GetMapping("/{id}/players")
    public ResponseEntity<List<Player>> findPlayersFromGame(@PathVariable("id") Integer id) throws ResourceNotFoundException {
        List<Player> l = matchService.getPlayersFromGame(id);
        if(l == null) {
            return new ResponseEntity<>(List.of(), HttpStatus.NOT_FOUND);
        } else return new ResponseEntity<>(l, HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Match> findByName(@PathVariable("name")  String name) throws ResourceNotFoundException {
        Match p = matchService.geByName(name);
        if (p == null)
            return new ResponseEntity<>(p, HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<>(p, HttpStatus.OK);
    }


    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("ADMIN")
    public void deleteAll() {
        matchService.deleteAll();
    }

    @DeleteMapping(value = "{id}")
	@ResponseStatus(HttpStatus.OK)
    @PreAuthorize("ADMIN")
	public ResponseEntity<Object> deleteById (@PathVariable("id") Integer id) throws ErrorMessage {
		Match p = matchService.getById(id);
        RestPreconditions.checkNotNull(p, "Partida", "id", id);
		if (p != null) {
			matchService.deletePartidaById(id);
			return new ResponseEntity<>(new MessageResponse("Partida borrada"), HttpStatus.NO_CONTENT);
		} else
			return new ResponseEntity<>(new ErrorMessage(422, new Date(), 
            "La Partida no pudo ser borrada", "Probablemente no existiese"), HttpStatus.NOT_FOUND) ;
	}

    @DeleteMapping(value = "/ids/{ids}")
	@ResponseStatus(HttpStatus.OK)
    @PreAuthorize("ADMIN")
	public ResponseEntity<Object> deleteSomeById (@PathVariable("ids") List<Integer> ids) throws ErrorMessage {
		List<Integer> idsPartidasNoBorradas = new LinkedList<>();
        Integer numPartidasEncontradas = 0;
        for (Integer id : ids) {
            RestPreconditions.checkNotNull(matchService.getById(id), "Partida", "ID", id);
		if (matchService.getById(id) != null) {
            numPartidasEncontradas++;
		} else
            idsPartidasNoBorradas.add(id);
        }
        if (numPartidasEncontradas == ids.size()) {
            matchService.deleteSomeById(ids);
            return new ResponseEntity<>(new MessageResponse("Partidas borradas"), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(idsPartidasNoBorradas, HttpStatus.NOT_FOUND);
            // new ErrorMessage(422, new Date(), "La Partida no pudo ser borrada", "Probablemente no existiese")
        }
        
	}


    @PutMapping("/{id}")
    public ResponseEntity<Match> updateById(@PathVariable("id") Integer idToUpdate, @RequestBody @Valid Match partidaNueva) {
        try {
            RestPreconditions.checkNotNull(matchService.getById(idToUpdate), "Partida", "ID", idToUpdate);
            return new ResponseEntity<>(matchService.updateById(partidaNueva, idToUpdate), HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }    


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Match> create(@RequestBody @Valid Match partida) {
        try {
            if (partida == null || partida.getName() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (partida.getName().length() < 3 || partida.getName().length() > 50) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(matchService.save(partida), HttpStatus.CREATED);
        } catch (DataAccessException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PatchMapping("/{matchId}/actualPlayer/{playerId}")
    public ResponseEntity<Match> updateJugadorActual(@PathVariable("matchId") Integer matchId, @PathVariable("playerId") Integer playerId) throws ResourceNotFoundException {
        Match partida = matchService.getById(matchId);

        if (partida == null) {
            throw new ResourceNotFoundException("Partida no encontrada", "id", matchId.toString());
        }
        Player j = playerService.getJugadorById(playerId);
        partida.setActualPlayer(j);
        matchService.save(partida);
        return new ResponseEntity<>(partida, HttpStatus.OK);
    }

    @PatchMapping("/{matchId}/ronda")
    public ResponseEntity<Match> updateRound(@PathVariable("matchId") Integer matchId) throws ResourceNotFoundException {
        Match partida = matchService.getById(matchId);
        if (partida == null) {
            throw new ResourceNotFoundException("Partida no encontrada", "id", matchId.toString());
        }
        partida.setRound(partida.getRound()+1);
        return new ResponseEntity<>(partida, HttpStatus.OK);
    }

    @PostMapping("/matchCreator/{userId}")
    public ResponseEntity<Match> createMatchWMatchCreator(@PathVariable("userId") Integer userId, @RequestBody Map<String, String> requestBody) { 
    User u = userService.findUser(userId);
    if (u == null) return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    String name = requestBody.getOrDefault("name", "");
    String password = requestBody.getOrDefault("password", "");
    Match m = new Match();

    m.setName(name);
    m.setPassword(password);
    m.setMatchCreator(u);
    m.setState(State.ESPERANDO);
    m.setPlayersNum(0);
    m.setRound(0);
    m.setPhase(Phase.CASILLAS);
    m.setInitialPlayer(null);
    m.setActualPlayer(null);
    matchService.save(m);

    return new ResponseEntity<>(m, HttpStatus.CREATED);
}

    @PatchMapping("/{matchId}/changephase")
    public ResponseEntity<Match> changePhase(@PathVariable("matchId") Integer matchId) {
        Match p = matchService.getById(matchId);
        Phase f = p.getPhase();
        List<MatchTile> mt = matchTileService.findByMatchId(matchId);
        List<SalmonMatch> sm = sms.getAllFromMatch(matchId);
        if(f.equals(Phase.CASILLAS)) p.setPhase(Phase.MOVIENDO);
        else {
            List<MatchTile> garzas = mt.stream().filter(m -> m.getTile().getType().getType().equals("GARZA")).toList();
            for(MatchTile g:garzas) {
                for(SalmonMatch s:sm) {
                    if(g.getCoordinate().equals(s.getCoordinate())) {
                        s.setSalmonsNumber(s.getSalmonsNumber()-1);
                        if(s.getSalmonsNumber().equals(0)) sms.delete(s.getId());
                        else sms.savePartidaSalmon(s);
                    }
                }    
            }
            p.setPhase(Phase.CASILLAS);
            p.setRound(p.getRound()+1);
            
        }
        matchService.save(p);
        return new ResponseEntity<>(p, HttpStatus.OK);
    }

    @PatchMapping("/{matchId}/startGame")
    public ResponseEntity<Match> startGame(@PathVariable("matchId") Integer matchId) throws ResourceNotFoundException {
        Match m = matchService.getById(matchId);
        List<Player> p = playerService.getPlayersByMatch(matchId);

        m.setState(State.EN_CURSO);
        m.setActualPlayer(p.get(0));
        m.setInitialPlayer(p.get(0));
        m.setNumJugadores(p.size());
        matchService.save(m);
        return new ResponseEntity<>(m, HttpStatus.OK);
    }


}