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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.auth.payload.response.MessageResponse;
import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ErrorMessage;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.salmonMatchService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import es.us.dp1.l4_01_24_25.upstream.util.RestPreconditions;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchRestController {
    
    private final MatchService matchService;
    private final PlayerService playerService;
    private final UserService userService;
    private final MatchTileService matchTileService;
    private final salmonMatchService salmonMatchService;

    public MatchRestController(MatchService partidaService, PlayerService jugadorService, UserService userService, MatchTileService matchTileService, salmonMatchService sms) {
        this.matchService = partidaService;
        this.playerService = jugadorService;
        this.userService = userService;
        this.matchTileService = matchTileService;
        this.salmonMatchService = sms;
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
    public ResponseEntity<Match> updateJugadorActual(@PathVariable("matchId") Integer matchId, @PathVariable("playerId") Integer playerId) throws ResourceNotFoundException, Exception {
        Match partida = matchService.getById(matchId);
        Integer numPlayers = partida.getPlayersNum();
        Player p = playerService.getById(playerId);
        List<Player> players = playerService.getPlayersByMatch(matchId);
        if (partida == null || p == null) throw new ResourceNotFoundException("Partida no encontrada", "id", matchId.toString());
        Integer myOrder = p.getPlayerOrder();
        Player nextPlayer = p;
        if(partida.getPhase().equals(Phase.CASILLAS)) {
            if(partida.getRound().equals(0)) nextPlayer = players.stream().filter(pl -> pl.getPlayerOrder().equals((myOrder + 1)%numPlayers)).toList().get(0);
            }
        else if(partida.getPhase().equals(Phase.MOVIENDO)) {
            nextPlayer = players.stream().filter(pl -> pl.getPlayerOrder().equals((myOrder + 1)%numPlayers)).toList().get(0);
        }
        partida.setActualPlayer(nextPlayer);
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
        Match match = matchService.getById(matchId);
        Phase phase = match.getPhase();
        List<MatchTile> mtNoC = matchTileService.findByMatchIdNoCoord(matchId);
        List<Player> players = matchService.getPlayersFromGame(matchId);
        Integer round = match.getRound();
        if(phase.equals(Phase.CASILLAS)) {
            List<Integer> rds = List.of(17, 14, 11, 8, 5, 2);
            if (rds.contains(mtNoC.size())) {
                players.stream().forEach(p -> p.setEnergy(5));
                for(Player p : players) playerService.saveJugador(p);
                match.setPhase(Phase.MOVIENDO);
            }
        }
        else {
            if(players.stream().allMatch(p -> p.getEnergy() <= 0)) {
                /* 
                List<MatchTile> mt = matchTileService.findByMatchId(matchId);
                List<MatchTile> mtNoc = matchTileService.findByMatchIdNoCoord(matchId);
                List<SalmonMatch> salmonMatches = match.getSalmonMatches();
                if(mtNoc.size() == 11){
                    for(SalmonMatch sm:salmonMatches) {
                        if(sm.getCoordinate().equals(null)){
                            salmonMatchService.delete(sm.getId());
                        }
                    }
                }else if (mtNoc.size() == 0){
                    for(SalmonMatch sm:salmonMatches) {
                        if(sm.getCoordinate().y().equals(0)){
                            salmonMatchService.delete(sm.getId());
                        }
                    }
                    for(MatchTile m:mt) {
                        if(m.getCoordinate().y().equals(0)){
                            m.setCoordinate(new Coordinate(m.getCoordinate().x(), 99));
                        }
                        matchTileService.save(m);
                    }
                }else{
                    for(SalmonMatch sm:salmonMatches) {
                        if(sm.getCoordinate().y().equals(0)){
                            salmonMatchService.delete(sm.getId());
                        }
                    }
                    for(MatchTile m:mt) {
                        if(m.getCoordinate().y().equals(0)){
                            m.setCoordinate(new Coordinate(m.getCoordinate().x(), 99));
                        }else{
                            m.setCoordinate(new Coordinate(m.getCoordinate().x(), m.getCoordinate().y()-1));
                        }
                        matchTileService.save(m);
                    }
                }*/
                match.setPhase(Phase.CASILLAS);
                match.setRound(round+1);
            }
        }
        matchService.save(match);
        return new ResponseEntity<>(match, HttpStatus.OK);
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

    /*@GetMapping("/{salmon}/moveIsPossible")
    public ResponseEntity<Boolean> moveValid( @PathVariable("salmon") SalmonMatch salmonToMove, @RequestParam("x") int x, @RequestParam("y") int y) {
        Integer xToMove = Math.abs(x - salmonToMove.getCoordinate().x());
        Integer yToMove = Math.abs(y - salmonToMove.getCoordinate().y());
        if (xToMove == 2) {
            if (yToMove < 2) {
                return ResponseEntity.ok(false);
            }
        }
        return ResponseEntity.ok(y > salmonToMove.getCoordinate().y());
    }

    @GetMapping("/{salmon}/{playerId}/energyValid")
    public ResponseEntity<Boolean> energyValid(@PathVariable("salmon") SalmonMatch salmonToMove,@RequestParam("x") int x,
            @RequestParam("y") int y, @PathVariable("playerId") Integer playerId) {
        Player player = playerService.getById(playerId);
        if (moveValid(salmonToMove, x, y).getBody()) {
            Boolean isValid = y - salmonToMove.getCoordinate().y() <= player.getEnergy();
            return ResponseEntity.ok(isValid);
        } else {
            return ResponseEntity.ok(false);
        }
    }*/

    @PatchMapping("/{matchId}/threats/garza") 
    public ResponseEntity<List<SalmonMatch>> garzasThreat(@PathVariable("matchId") Integer matchId, @RequestParam("playerId") Integer playerId) {
        List<MatchTile> mt = matchTileService.findByMatchId(matchId);
        List<SalmonMatch> sm = playerService.getSalmonsByPlayerId(playerId);
        List<MatchTile> garzas = mt.stream().filter(m -> m.getTile().getType().getType().equals("GARZA")).toList();
            for(MatchTile g:garzas) {
                for(SalmonMatch salmon:sm) {
                    if(g.getCoordinate().equals(salmon.getCoordinate())) {
                        salmon.setSalmonsNumber(salmon.getSalmonsNumber()-1);
                        if(salmon.getSalmonsNumber().equals(0)) {
                            salmonMatchService.delete(salmon.getId()); 
                            sm.remove(salmon);
                        }
                        else salmonMatchService.savePartidaSalmon(salmon);
                    }
                }    
            }
        return ResponseEntity.ok(sm);
    }

    @PatchMapping("/{matchId}/endRound")
    public ResponseEntity<Match> endRound(@PathVariable("matchId") Integer matchId) throws ResourceNotFoundException {
        Match partida = matchService.getById(matchId);
        List<MatchTile> mt = matchTileService.findByMatchId(matchId);
        List<Integer> rds = List.of(17, 14, 11, 8, 5, 2);
        List<MatchTile> mtNoc = matchTileService.findByMatchIdNoCoord(matchId);
        List<SalmonMatch> salmonMatches = salmonMatchService.getAllFromMatch(matchId);
        Integer round = partida.getRound();
        Phase phase = partida.getPhase();

        
        if(round == 2){ 
            List<SalmonMatch> mtInOcean = salmonMatches.stream().filter(m -> m.getCoordinate()==null).toList();
            for (SalmonMatch sm: mtInOcean) { 
                eliminarSalmon(sm.getId());
            }
           
        }else if (round > 2 && mtNoc.size() == 0){
            List<SalmonMatch> mtOutOfPosition = salmonMatches.stream().filter(m -> m.getCoordinate().y() == 0).toList();
            for(SalmonMatch sm:mtOutOfPosition) {
                eliminarSalmon(sm.getId());
            } 
            for(MatchTile m:mt) {
                if(m.getCoordinate().y()==0){
                    matchTileService.deleteMatchTile(m.getId());
                }
            
            }
            matchTileService.findByMatchId(matchId).stream()
            .filter(mT -> mT.getCoordinate() != null)
            .forEach(mT -> {
            Coordinate oldCoord = mT.getCoordinate();
             Coordinate newCoord = new Coordinate(oldCoord.x(), oldCoord.y() - 1); // Asumiendo constructor
             mT.setCoordinate(newCoord); // Actualiza la coordenada
        });
        }else if(round>2 && rds.contains(mtNoc.size()) && phase == Phase.CASILLAS){
            for(SalmonMatch sm:salmonMatches) {
                if(sm.getCoordinate().y()==0){
                    salmonMatchService.delete(sm.getId());
                }else{
                    //m.setCoordinate(new Coordinate(m.getCoordinate().x(), m.getCoordinate().y()-1));
                    Coordinate oldCoord = sm.getCoordinate();
                    Coordinate newCoord = new Coordinate(oldCoord.x(), oldCoord.y() - 1);
                    sm.setCoordinate(newCoord);
                    salmonMatchService.savePartidaSalmon(sm);

                }

            }

            for(MatchTile m:mt) {
                if(m.getCoordinate().y()==0){
                    matchTileService.deleteMatchTile(m.getId());
                    //m.setCoordinate(new Coordinate(m.getCoordinate().x(), 99));
                }else{
                    //m.setCoordinate(new Coordinate(m.getCoordinate().x(), m.getCoordinate().y()-1));
                    Coordinate oldCoord = m.getCoordinate();
                    Coordinate newCoord = new Coordinate(oldCoord.x(), oldCoord.y() - 1);
                    m.setCoordinate(newCoord);
                    matchTileService.save(m);

                }
                }
                //matchTileService.save(m);
            }
        
        return new ResponseEntity<>(partida, HttpStatus.OK);
    }


    private void eliminarSalmon(Integer id){
        salmonMatchService.delete(id);
    }
}