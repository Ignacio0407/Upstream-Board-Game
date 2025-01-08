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
import es.us.dp1.l4_01_24_25.upstream.exceptions.NoCapacityException;
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
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/salmonMatches")
@SecurityRequirement(name = "bearerAuth")
public class salmonMatchController {

    private final salmonMatchService salmonMatchService;
    private final PlayerService playerService;
    private final SalmonService salmonService;
    private final MatchTileService matchTileService;
    private final MatchService matchService;

    @Autowired
    public salmonMatchController(salmonMatchService salmonMatchService, PlayerService playerService, SalmonService salmonService, MatchTileService matchTileService, MatchService matchService) {
        this.salmonMatchService = salmonMatchService;
        this.playerService = playerService;
        this.salmonService = salmonService;
        this.matchTileService = matchTileService;
        this.matchService = matchService;
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<salmonMatch>> findAllFromMatch(@PathVariable Integer matchId) {  
        return new ResponseEntity<>(salmonMatchService.getAllFromMatch(matchId), HttpStatus.OK);
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<salmonMatch>> findAllFromPlayer(@PathVariable Integer playerId) {  
        return new ResponseEntity<>(salmonMatchService.getAllFromPlayer(playerId), HttpStatus.OK);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<salmonMatch> findById(@PathVariable("id") Integer id){
        return new ResponseEntity<>(salmonMatchService.getById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<salmonMatch> create(@RequestBody @Valid salmonMatch salmonMatch) {
        return new ResponseEntity<>(salmonMatchService.save(salmonMatch), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        salmonMatchService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/coordinate/{id}")
    public ResponseEntity<salmonMatch> updateCoordinate(@PathVariable Integer id, @RequestBody @Valid  Map<String,Integer> coordinate) throws NotValidMoveException,  InsufficientEnergyException, OnlyMovingForwardException, NoCapacityException {
        salmonMatch salmonMatch = salmonMatchService.getById(id);
        Player player = salmonMatch.getPlayer();
        Match match = salmonMatch.getMatch();
        List<Player> players = playerService.getPlayersByMatch(match.getId());
        Integer numPlayers = match.getPlayersNum();
        Coordinate myCoordinate = salmonMatch.getCoordinate();
        Coordinate newCoordinate = new Coordinate(coordinate.get("x"), coordinate.get("y"));
        List<MatchTile> matchTiles = matchTileService.findByMatchId(match.getId());
        MatchTile toTravel = matchTiles.stream().filter(mT -> mT.getCoordinate() != null && mT.getCoordinate().equals(newCoordinate)).toList().get(0);
        String toTravelType = toTravel.getTile().getType().getType();
        Integer salmonsInToTravel =  salmonMatchService.getAllFromMatch(match.getId()).stream().filter(s -> s.getCoordinate() != null && s.getCoordinate().equals(newCoordinate)).toList().size();
        Integer energyUsed = 1;
        
        if (toTravel.getCapacity() < salmonsInToTravel + 1) throw new NoCapacityException("No queda hueco en esta casilla");

        if(myCoordinate != null && myCoordinate.equals(newCoordinate)) throw new NotValidMoveException("No puedes moverte en la misma casilla");

        if (player.getEnergy() <= 0) throw new InsufficientEnergyException("No energía crack");

        if (myCoordinate == null && newCoordinate.y() != 0) throw new NotValidMoveException("Solo puedes moverte de uno en uno"); 

        // Si salgo desde el desove.
        else if (myCoordinate == null) {
            if(toTravelType.equals("OSO") && List.of(0, 1).contains(toTravel.getOrientation())) {
                energyUsed = 2;
                salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber() - 1);
            }
            else if(toTravelType.equals("SALTO") && List.of(0, 1, 5).contains(toTravel.getOrientation())) {
                energyUsed = 2;
            }
        }

        // Si ya estoy en el tablero.
        else if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
            MatchTile myTile = matchTiles.stream().filter(mt -> mt.getCoordinate() != null && myCoordinate != null && mt.getCoordinate().equals(myCoordinate)).toList().get(0);
            String myCoordinateType = myTile.getTile().getType().getType();
            Coordinate distancia = new Coordinate((newCoordinate.x() - myCoordinate.x()), (newCoordinate.y() - myCoordinate.y()));
            
            if(distancia.y() < 0) throw new NotValidMoveException("Solo puedes moverte hacia delante"); 

            if(myCoordinate.y().equals(newCoordinate.y()) && myCoordinate.x().equals(1)) throw new NotValidMoveException("Solo puedes moverte hacia delante");

            if(List.of(0, 2).contains(myCoordinate.x()) && Math.abs(distancia.x()) == 1 && Math.abs(distancia.y()) == 1) throw new NotValidMoveException("Este movimiento no está permitido");    

            // Si subo, independientemente de dónde esté.
            if(newCoordinate.y() == myCoordinate.y() + 1 && myCoordinate.x() == newCoordinate.x()) { 
                if(myCoordinateType.equals("OSO") && List.of(3,4).contains(myTile.getOrientation()) || 
            toTravelType.equals("OSO") && List.of(0,1).contains(toTravel.getOrientation())) {
                salmonMatch.setSalmonsNumber((myCoordinateType.equals(toTravelType))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
                energyUsed = 2;    
            }
            else if(myCoordinateType.equals("SALTO") && List.of(2,3,4).contains(myTile.getOrientation()) ||
            toTravelType.equals("SALTO") && List.of(0,1,5).contains(toTravel.getOrientation())) {
                salmonMatch.setSalmonsNumber((myCoordinateType.equals("OSO"))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                energyUsed = 2;    
            }
                
            }

            // Si me muevo a la derecha, independientemente de donde esté.
            else if(newCoordinate.x().equals(myCoordinate.x() + 1)) {
                if(myCoordinateType.equals("OSO") && List.of(4,5).contains(myTile.getOrientation()) || 
                    toTravelType.equals("OSO") && List.of(1,2).contains(toTravel.getOrientation())) {
                        salmonMatch.setSalmonsNumber((myCoordinateType.equals(toTravelType))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
                        energyUsed = 2;
                    }
                    else if(myCoordinateType.equals("SALTO") && List.of(3,4,5).contains(myTile.getOrientation()) ||
                    toTravelType.equals("SALTO") && List.of(0,1,2).contains(toTravel.getOrientation())) {
                        salmonMatch.setSalmonsNumber((myCoordinateType.equals("OSO"))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                        energyUsed = 2;
                    }    
                    
            }

            // Si me muevo a la izquierda, independientemente de donde esté.
            else if(newCoordinate.x().equals(myCoordinate.x()-1)) {
                    if(myCoordinateType.equals("OSO") && List.of(2,3).contains(myTile.getOrientation()) || 
                    toTravelType.equals("OSO") && List.of(0,5).contains(toTravel.getOrientation())) {
                        salmonMatch.setSalmonsNumber((myCoordinateType.equals(toTravelType))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
                        energyUsed = 2;
                    }
                    else if(myCoordinateType.equals("SALTO") && List.of(1,2,3).contains(myTile.getOrientation()) ||
                    toTravelType.equals("SALTO") && List.of(0,4,5).contains(toTravel.getOrientation())) {
                        salmonMatch.setSalmonsNumber((myCoordinateType.equals("OSO"))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                        energyUsed = 2;
                    }
                }
            }

        else throw new NotValidMoveException("Solo puedes moverte de uno en uno"); 

        salmonMatch.setCoordinate(newCoordinate);
        player.setEnergy(player.getEnergy() - energyUsed);   

        if(player.getEnergy() == 0) {
            Integer myOrder = player.getPlayerOrder();
            Player nextPlayer = players.stream().filter(pl -> pl.getPlayerOrder().equals((myOrder + 1)%numPlayers)).toList().get(0);
            match.setActualPlayer(nextPlayer);
            matchService.save(match);
        }
        
        playerService.savePlayer(player);
        matchTileService.save(toTravel);
        if (salmonMatch.getSalmonsNumber() > 0) salmonMatchService.save(salmonMatch);
        else salmonMatchService.delete(salmonMatch.getId());
        return new ResponseEntity<>(salmonMatch, HttpStatus.OK);
    }

    @PostMapping("/player/{playerId}")
    public void create(@PathVariable("playerId") Integer playerId) {
        for (int i=0; i < 4; i++) {
            Player p = playerService.getById(playerId);
            Salmon s = salmonService.findAll().stream().filter(sal -> sal.getColor().equals(p.getColor())).toList().get(0);
            Match m = p.getMatch();
            Coordinate c = new Coordinate(null, null);;
            salmonMatch r = new salmonMatch();
            r.setPlayer(p);
            r.setSalmonsNumber(2);
            r.setSpawningNumber(0);
            r.setCoordinate(c);
            r.setSalmon(s);
            r.setMatch(m);
            salmonMatchService.save(r);
        }
    }

}