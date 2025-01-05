package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.InsufficientEnergyException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.OnlyMovingForwardException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import es.us.dp1.l4_01_24_25.upstream.salmon.SalmonService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/salmonMatches")
@SecurityRequirement(name = "bearerAuth")
public class SalmonMatchController {

    private final salmonMatchService salmonMatchService;
    private final PlayerService playerService;
    private final SalmonService salmonService;
    private final MatchTileService matchTileService;
    private final MatchService matchService;

    @Autowired
    public SalmonMatchController(salmonMatchService salmonMatchService, PlayerService playerService, SalmonService salmonService, MatchTileService matchTileService, MatchService matchService) {
        this.salmonMatchService = salmonMatchService;
        this.playerService = playerService;
        this.salmonService = salmonService;
        this.matchTileService = matchTileService;
        this.matchService = matchService;
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<SalmonMatch>> findAllFromMatch(@PathVariable Integer matchId) {  
        return new ResponseEntity<>(salmonMatchService.getAllFromMatch(matchId), HttpStatus.OK);
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<SalmonMatch>> findAllFromPlayer(@PathVariable Integer playerId) {  
        return new ResponseEntity<>(salmonMatchService.getAllFromPlayer(playerId), HttpStatus.OK);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<SalmonMatch> findById(@PathVariable("id") Integer id){
        return new ResponseEntity<>(salmonMatchService.getPartidaSalmon(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<SalmonMatch> create(@RequestBody @Valid SalmonMatch salmonMatch) {
        return new ResponseEntity<>(salmonMatchService.savePartidaSalmon(salmonMatch), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        salmonMatchService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /*@PatchMapping("/coordinate/{id}")
    public ResponseEntity<SalmonMatch> updateCoordinate(@PathVariable("id") Integer id, @RequestBody @Valid  Map<String,Integer> coordinate) throws NotValidMoveException,  InsufficientEnergyException, OnlyMovingForwardException {
        SalmonMatch salmonMatch = salmonMatchService.getPartidaSalmon(id);
        Player player = salmonMatch.getPlayer();
        Match match = salmonMatch.getMatch();
        Integer numPlayers = match.getPlayersNum();
        List<Player> players = playerService.getPlayersByMatch(match.getId());
        Coordinate myCoordinate = salmonMatch.getCoordinate();
        Coordinate newCoordinate = new Coordinate(coordinate.get("x"), coordinate.get("y"));
        MatchTile destinyTile = matchTileService.findByCoordinate(newCoordinate.x(), newCoordinate.y());
        if (player.getEnergy() == 0) throw new InsufficientEnergyException("¡No te queda energía!");
        if (myCoordinate == null && newCoordinate.y() != 0) throw new NotValidMoveException("Solo puedes moverte de uno en uno"); 
        else if (myCoordinate == null) {
            if (destinyTile.getJumpingSides().contains(0)) player.setEnergy(player.getEnergy() - 2);
            if (destinyTile.getTile().getType().getType().equals("OSO")) {
                salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1); 
            }
            if (destinyTile.getTile().getType().getType().equals("AGUILA")) {
                salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
            }
            else player.setEnergy(player.getEnergy() - 1);
            salmonMatch.setCoordinate(newCoordinate);
        }
        else if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
            MatchTile myTile = matchTileService.findByCoordinate(myCoordinate.x(), myCoordinate.y());
            Coordinate distancia = new Coordinate((newCoordinate.x() - myCoordinate.x()), (newCoordinate.y() - myCoordinate.y()));
            if(distancia.y() < 0) throw new OnlyMovingForwardException("Solo puedes moverte hacia delante"); 
            if(myCoordinate.x() == 1) {
                if(myCoordinate.y().equals(newCoordinate.y())) throw new OnlyMovingForwardException("Solo puedes moverte hacia delante");
            }
            if(Math.abs(distancia.x()) == 2 && Math.abs(distancia.y()) == 1) throw new NotValidMoveException("Este movimiento no está permitido"); 
                // Si salto a esa casilla
                else if ( ( ( distancia.x() == 0 && destinyTile.getJumpingSides().contains(1) )
                        || ( distancia.x() == 1 && destinyTile.getJumpingSides().contains(2) )
                        || ( distancia.x() == -1 && destinyTile.getJumpingSides().contains(0) ) ) ) {
                        
                    if ( ( ( distancia.x() == 0 && myTile.getJumpingSides().contains(4) )
                        || ( distancia.x() == 1 && myTile.getJumpingSides().contains(5) )
                        || ( distancia.x() == -1 && myTile.getJumpingSides().contains(3) ) ) ) {
                            if (player.getEnergy() >= 3) player.setEnergy(player.getEnergy() - 3);
                            else throw new InsufficientEnergyException("¡No tienes los 3 puntos de energía requeridos para este movimiento!"); 
                    }
                    else if (player.getEnergy() >= 2) {
                        if (destinyTile.getTile().getType().getType().equals("OSO")) {
                            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                            if (salmonMatch.getSalmonsNumber() == 0) salmonMatchService.delete(id);
                            if (salmonMatchService.getPartidaSalmon(id) != null && myTile.getTile().getType().getType().equals("OSO")) {
                                salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                                if (salmonMatch.getSalmonsNumber() == 0) salmonMatchService.delete(id);
                            }
                        }
                        player.setEnergy(player.getEnergy() - 2);
                    }
                    else  throw new InsufficientEnergyException("¡No tienes los 2 puntos de energía requeridos para este movimiento!"); 
                }

                // Si salto desde mi casilla
                else if ( ( ( distancia.x() == 0 && myTile.getJumpingSides().contains(4) )
                || ( distancia.x() == 1 && myTile.getJumpingSides().contains(5) )
                || ( distancia.x() == -1 && myTile.getJumpingSides().contains(3) ) ) ) {
                    if (player.getEnergy() >= 2) player.setEnergy(player.getEnergy() - 2);
                    else throw new InsufficientEnergyException("¡No tienes los 2 puntos de energía requeridos para este movimiento!"); 
                }

                else if (destinyTile.getTile().getType().getType().equals("AGUILA")) {
                    salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                    if (salmonMatch.getSalmonsNumber() == 0) salmonMatchService.delete(id);
                    player.setEnergy(player.getEnergy() - 1);
                    if(player.getEnergy() == 0) {
                        Integer myOrder = player.getPlayerOrder();
                        Player nextPlayer = players.stream().filter(pl -> pl.getPlayerOrder().equals((myOrder + 1)%numPlayers)).toList().get(0);
                        match.setActualPlayer(nextPlayer);
                        matchService.save(match);
                    }
                }
                /*else {
                    player.setEnergy(player.getEnergy() - 1);
                    if(player.getEnergy() == 0) {
                        Integer myOrder = player.getPlayerOrder();
                        Player nextPlayer = players.stream().filter(pl -> pl.getPlayerOrder().equals((myOrder + 1)%numPlayers)).toList().get(0);
                        match.setActualPlayer(nextPlayer);
                        matchService.save(match);
                    }
                }
                salmonMatch.setCoordinate(newCoordinate);
            }
        if(player.getEnergy() == 0) {
            Integer myOrder = player.getPlayerOrder();
            Player nextPlayer = players.stream().filter(pl -> pl.getPlayerOrder().equals((myOrder + 1)%numPlayers)).toList().get(0);
            match.setActualPlayer(nextPlayer);
            matchService.save(match);
        }
        playerService.saveJugador(player);
        if (salmonMatch.getSalmonsNumber() == 0) salmonMatchService.delete(id); 
        else salmonMatchService.savePartidaSalmon(salmonMatch);
        return new ResponseEntity<>(salmonMatch, HttpStatus.OK);
    }*/
    @PatchMapping("/coordinate/{id}")
    public ResponseEntity<SalmonMatch> updateCoordinate(@PathVariable Integer id, @RequestBody @Valid  Map<String,Integer> coordinate) throws NotValidMoveException,  InsufficientEnergyException, OnlyMovingForwardException {
        SalmonMatch salmonMatch = salmonMatchService.getPartidaSalmon(id);
        Player player = salmonMatch.getPlayer();
        Match match = salmonMatch.getMatch();
        List<Player> players = playerService.getPlayersByMatch(match.getId());
        Integer numPlayers = match.getPlayersNum();
        Coordinate myCoordinate = salmonMatch.getCoordinate();
        Coordinate newCoordinate = new Coordinate(coordinate.get("x"), coordinate.get("y")); 
        if (player.getEnergy() == 0) throw new InsufficientEnergyException("No energía crack");
        if (myCoordinate == null && newCoordinate.y() != 0) throw new NotValidMoveException("Solo puedes moverte de uno en uno"); 
        else if (myCoordinate == null) {
            salmonMatch.setCoordinate(newCoordinate);
            player.setEnergy(player.getEnergy() - 1);
        }
        else if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
            Coordinate distancia = new Coordinate((newCoordinate.x() - myCoordinate.x()), (newCoordinate.y() - myCoordinate.y()));
            if(distancia.y() < 0) throw new NotValidMoveException("Solo puedes moverte hacia delante"); 
            if(myCoordinate.x() == 1) {
                if(myCoordinate.y().equals(newCoordinate.y())) throw new NotValidMoveException("Solo puedes moverte hacia delante");
                else{
                    salmonMatch.setCoordinate(newCoordinate);
                    player.setEnergy(player.getEnergy() - 1);
                }
            }
            else {
                if(Math.abs(distancia.x()) == 1 && Math.abs(distancia.y()) == 1) throw new NotValidMoveException("Este movimiento no está permitido");
                else {
                    salmonMatch.setCoordinate(newCoordinate);
                    player.setEnergy(player.getEnergy() - 1);
                }
            }
        }
        else throw new NotValidMoveException("Solo puedes moverte de uno en uno"); 
        if(player.getEnergy() == 0) {
            Integer myOrder = player.getPlayerOrder();
            Player nextPlayer = players.stream().filter(pl -> pl.getPlayerOrder().equals((myOrder + 1)%numPlayers)).toList().get(0);
            match.setActualPlayer(nextPlayer);
            matchService.save(match);
        }
        playerService.saveJugador(player);
        salmonMatchService.savePartidaSalmon(salmonMatch);
        return new ResponseEntity<>(salmonMatch, HttpStatus.OK);
    }

    @PostMapping("/player/{playerId}")
    public void create(@PathVariable("playerId") Integer playerId) {
        for (int i=0; i < 4; i++) {
            Player p = playerService.getJugadorById(playerId);
            Salmon s = salmonService.findAll().stream().filter(sal -> sal.getColor().equals(p.getColor())).toList().get(0);
            Match m = p.getMatch();
            Coordinate c = new Coordinate(null, null);;
            SalmonMatch r = new SalmonMatch();
            r.setPlayer(p);
            r.setSalmonsNumber(2);
            r.setSpawningNumber(0);
            r.setCoordinate(c);
            r.setSalmon(s);
            r.setMatch(m);
            salmonMatchService.savePartidaSalmon(r);
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        salmonMatchService.delete(id);
    }
}
