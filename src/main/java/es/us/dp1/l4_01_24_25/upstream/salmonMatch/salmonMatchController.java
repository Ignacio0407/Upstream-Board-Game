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
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
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

    private final SalmonMatchService salmonMatchService;
    private final PlayerService playerService;
    private final SalmonService salmonService;
    private final MatchTileService matchTileService;
    private final MatchService matchService;

    @Autowired
    public SalmonMatchController(SalmonMatchService salmonMatchService, PlayerService playerService, SalmonService salmonService, MatchTileService matchTileService, MatchService matchService) {
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

    
    @GetMapping("/match/{matchId}/spawn")
    public ResponseEntity<List<SalmonMatch>> findAllFromMatchInSpawn(@PathVariable Integer matchId) {  
        return new ResponseEntity<>(salmonMatchService.getSalmonsInSpawnFromGame(matchId), HttpStatus.OK);
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<SalmonMatch>> findAllFromPlayer(@PathVariable Integer playerId) {  
        return new ResponseEntity<>(salmonMatchService.getAllFromPlayer(playerId), HttpStatus.OK);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<SalmonMatch> findById(@PathVariable("id") Integer id){
        return new ResponseEntity<>(salmonMatchService.getById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<SalmonMatch> create(@RequestBody @Valid SalmonMatch salmonMatch) {
        return new ResponseEntity<>(salmonMatchService.save(salmonMatch), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        salmonMatchService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void throwExceptions(MatchTile toTravel, Integer salmonsInToTravel, Coordinate myCoordinate, Coordinate newCoordinate, Player player) {
        // if (toTravel.getCapacity() < salmonsInToTravel + 1) throw new NoCapacityException("No queda hueco en esta casilla");
 
         if(myCoordinate != null && myCoordinate.equals(newCoordinate)) throw new NotValidMoveException("No puedes moverte en la misma casilla");
 
         if (player.getEnergy() <= 0) throw new InsufficientEnergyException("No hay suficiente energía para ese movimiento");
 
         if (myCoordinate == null && newCoordinate.y() != 0) throw new NotValidMoveException("Solo puedes moverte de uno en uno"); 
 
    }

    private MatchTile tileFullNull(SalmonMatch salmonMatch, List<MatchTile> matchTiles, MatchTile toTravel2, Coordinate newCoordinate, Coordinate newCoordinate2, Integer energyUsed) {
        newCoordinate2 = new Coordinate(newCoordinate.x(), newCoordinate.y()+1);
        Coordinate newCoordinateAux = new Coordinate(newCoordinate2.x(), newCoordinate2.y());
        toTravel2 = matchTiles.stream().filter(mT -> mT.getCoordinate() != null && mT.getCoordinate().equals(newCoordinateAux)).toList().get(0);
        String toTravelType2 = toTravel2.getTile().getType().getType();
        if(toTravelType2.equals("OSO") && List.of(0, 1).contains(toTravel2.getOrientation())) 
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber() - 1);
        return toTravel2;
    }
    
    private MatchTile handleTileFull(Coordinate newCoordinate2, Coordinate newCoordinate, SalmonMatch salmonMatch, MatchTile myTile, 
        String myCoordinateType, List<MatchTile> matchTiles, List<Integer> from, List<Integer> to, Integer x, Integer y) {
        newCoordinate2 = new Coordinate(newCoordinate.x()+x, newCoordinate.y() + y);
        Coordinate newCoordinateAux = new Coordinate(newCoordinate2.x(), newCoordinate2.y());
        MatchTile toTravel2 = matchTiles.stream().filter(mT -> mT.getCoordinate() != null && mT.getCoordinate().equals(newCoordinateAux)).findFirst().get();
        String toTravelType2 = toTravel2.getTile().getType().getType();
        if (osoBoolean(myTile, toTravel2, myCoordinateType, toTravelType2, List.of(2,3), List.of(0,5))) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber() - 1);
        }
        toTravel2.setSalmonsNumber(toTravel2.getSalmonsNumber() + 1);
        matchTileService.save(toTravel2);
        return toTravel2;
    }

    private Coordinate newCoordinateToTravel(Coordinate newCoordinate, Integer x, Integer y) {
        return new Coordinate(newCoordinate.x()+x, newCoordinate.y() + y);
    }

    private void changePlayer(Player player, Match match, List<Player> players, Integer numPlayers) {
        Integer myOrder = player.getPlayerOrder();
        Player nextPlayer = players.stream().filter(pl -> pl.getPlayerOrder().equals((myOrder + 1)%numPlayers)).findFirst().orElse(null);
        match.setActualPlayer(nextPlayer);
        matchService.save(match);
    }

    private Boolean osoBoolean(MatchTile myTile, MatchTile toTravel, String myCoordinateType, String toTravelType, List<Integer> from, List<Integer> to) {
        return myCoordinateType.equals("OSO") && from.contains(myTile.getOrientation()) ||
            toTravelType.equals("OSO") && to.contains(toTravel.getOrientation());
    }

    private Boolean saltoBoolean(MatchTile myTile, MatchTile toTravel, String myCoordinateType, String toTravelType, List<Integer> from, List<Integer> to) {
        return myCoordinateType.equals("SALTO") && from.contains(myTile.getOrientation()) ||
            toTravelType.equals("SALTO") && to.contains(toTravel.getOrientation());
    }

    @PatchMapping("/coordinate/{id}")
    public ResponseEntity<SalmonMatch> updateCoordinate(@PathVariable Integer id, @RequestBody @Valid  Map<String,Integer> coordinate) throws NotValidMoveException,  InsufficientEnergyException, OnlyMovingForwardException, NoCapacityException {
        SalmonMatch salmonMatch = salmonMatchService.getById(id);
        Player player = salmonMatch.getPlayer();
        Match match = salmonMatch.getMatch();
        List<Player> players = playerService.getPlayersByMatch(match.getId());
        Integer numPlayers = match.getPlayersNum();
        Coordinate myCoordinate = salmonMatch.getCoordinate();
        Coordinate newCoordinate = new Coordinate(coordinate.get("x"), coordinate.get("y"));
        List<MatchTile> matchTiles = matchTileService.findByMatchId(match.getId());
        MatchTile toTravel = matchTiles.stream().filter(mT -> mT.getCoordinate() != null && mT.getCoordinate().equals(newCoordinate)).findFirst().orElse(null);
        String toTravelType = toTravel.getTile().getType().getType();
        Coordinate newCoordinate2 = null;
        MatchTile toTravel2 = null;
        Integer salmonsInToTravel = salmonMatchService.getAllFromMatch(match.getId()).stream().filter(s -> s.getCoordinate() != null && s.getCoordinate().equals(newCoordinate)).toList().size();
        Integer energyUsed = 1;

        throwExceptions(toTravel, salmonsInToTravel, myCoordinate, newCoordinate, player);

        // Si salgo desde el mar.
        if (myCoordinate == null) {
            if (toTravel.getCapacity().equals(toTravel.getSalmonsNumber()) ) {
                toTravel2 = tileFullNull(salmonMatch, matchTiles, toTravel2, newCoordinate, newCoordinate2, energyUsed);
                if (!toTravel2.getCapacity().equals(toTravel.getSalmonsNumber())) 
                    newCoordinate2 = new Coordinate(newCoordinate.x(), newCoordinate.y()+1);
                else throw new NotValidMoveException("¡La casilla siguiente también está llena!");
                energyUsed = 3;
            }
            else if(toTravelType.equals("SALTO") && List.of(0, 1, 5).contains(toTravel.getOrientation())) energyUsed = 2;
            else if(toTravelType.equals("AGUILA")) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
            toTravel = matchTileService.eagleToWater(toTravel, match);
            } 
        }

        // Si ya estoy en el tablero.
        else if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
            MatchTile myTile = matchTiles.stream().filter(mt -> mt.getCoordinate() != null && mt.getCoordinate().equals(myCoordinate)).findFirst().orElse(null);
            String myCoordinateType = myTile.getTile().getType().getType();
            Coordinate distancia = new Coordinate((newCoordinate.x() - myCoordinate.x()), (newCoordinate.y() - myCoordinate.y()));
            myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
            matchTileService.save(myTile);
            
            if(distancia.y() < 0) throw new NotValidMoveException("Solo puedes moverte hacia delante"); 

            if(myCoordinate.y().equals(newCoordinate.y()) && myCoordinate.x().equals(1)) throw new NotValidMoveException("Solo puedes moverte hacia delante");

            if(List.of(0, 2).contains(myCoordinate.x()) && Math.abs(distancia.x()) == 1 && Math.abs(distancia.y()) == 1) throw new NotValidMoveException("Este movimiento no está permitido");    

            if (toTravel.getCapacity().equals(toTravel.getSalmonsNumber())) {
                energyUsed = 3;
                if (null != myCoordinate.x()) switch (myCoordinate.x()) {
                    case 1 -> {
                        // Si estoy en el centro
                        if (myCoordinate.y().equals(newCoordinate.y())) throw new NotValidMoveException("Solo puedes moverte hacia delante");
                        else if (newCoordinate.x().equals(myCoordinate.x())) { // Si me quedo en el centro
                            toTravel2 = handleTileFull(newCoordinate2, newCoordinate, salmonMatch, myTile, myCoordinateType, matchTiles, List.of(3, 4), List.of(0, 1), 0, 1);
                            if (!toTravel2.getCapacity().equals(toTravel.getSalmonsNumber())) 
                                newCoordinate2 = newCoordinateToTravel(newCoordinate, 0, 1);
                            else throw new NotValidMoveException("¡La casilla siguiente también está llena!");
                        }
                    }
                    case 0 -> {
                        // Si estoy en la izquierda
                        if (newCoordinate.y() == myCoordinate.y() + 1) { // Si subo
                            toTravel2 = handleTileFull(newCoordinate2, newCoordinate, salmonMatch, myTile, myCoordinateType, matchTiles, List.of(3, 4), List.of(0, 1), 0, 1);
                            if (!toTravel2.getCapacity().equals(toTravel.getSalmonsNumber())) 
                                newCoordinate2 = newCoordinateToTravel(newCoordinate, 0, 1);
                            else throw new NotValidMoveException("¡La casilla siguiente también está llena!");
                        } else if (newCoordinate.x() == myCoordinate.x() + 1) { // Si voy al centro
                            toTravel2 = handleTileFull(newCoordinate2, newCoordinate, salmonMatch, myTile, myCoordinateType, matchTiles, List.of(4, 5), List.of(1, 2), 1, 1);
                            if (!toTravel2.getCapacity().equals(toTravel.getSalmonsNumber())) 
                                newCoordinate2 = newCoordinateToTravel(newCoordinate, 1, 1);
                            else throw new NotValidMoveException("¡La casilla siguiente también está llena!");
                        }
                    }
                    case 2 -> {
                        // Si estoy en la derecha
                        if (newCoordinate.y() == myCoordinate.y() + 1) { // Si subo
                            toTravel2 = handleTileFull(newCoordinate2, newCoordinate, salmonMatch, myTile, myCoordinateType, matchTiles, List.of(3, 4), List.of(0, 1), 0, 1);
                            if (!toTravel2.getCapacity().equals(toTravel.getSalmonsNumber())) 
                                newCoordinate2 = newCoordinateToTravel(newCoordinate, 0, 1);
                            else throw new NotValidMoveException("¡La casilla siguiente también está llena!");
                        } else if (newCoordinate.x() == myCoordinate.x() - 1) { // Si voy al centro
                            toTravel2 = handleTileFull(newCoordinate2, newCoordinate, salmonMatch, myTile, myCoordinateType, matchTiles, List.of(2, 3), List.of(0, 5), -1, 1);
                            if (!toTravel2.getCapacity().equals(toTravel.getSalmonsNumber())) 
                                newCoordinate2 = newCoordinateToTravel(newCoordinate, -1, 1);
                            else throw new NotValidMoveException("¡La casilla siguiente también está llena!");
                        }
                    }
                    default -> {
                    }
                }
            }

            if(toTravelType.equals("AGUILA")) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
            toTravel = matchTileService.eagleToWater(toTravel, match);
        } 
            // Si subo, independientemente de dónde esté.
            if(newCoordinate.y() == myCoordinate.y() + 1 && myCoordinate.x().equals(newCoordinate.x())) { 
                if(osoBoolean(myTile, toTravel, myCoordinateType, toTravelType, List.of(3,4), List.of(0,1))) {
                salmonMatch.setSalmonsNumber((myCoordinateType.equals(toTravelType))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
                energyUsed = 2;    
                }
                else if(saltoBoolean(myTile, toTravel, myCoordinateType, toTravelType, List.of(2,3,4), List.of(0,1,5))) {
                    salmonMatch.setSalmonsNumber((myCoordinateType.equals("OSO"))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                    energyUsed = 2;    
                }                
            }

            // Si me muevo a la derecha, independientemente de donde esté.
            else if(newCoordinate.x().equals(myCoordinate.x() + 1)) {
                if(osoBoolean(myTile, toTravel, myCoordinateType, toTravelType, List.of(4,5), List.of(1,2))) {
                    salmonMatch.setSalmonsNumber((myCoordinateType.equals(toTravelType))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
                    energyUsed = 2;
                }
                else if(saltoBoolean(myTile, toTravel, myCoordinateType, toTravelType, List.of(3,4,5), List.of(0,1,2))) {
                    salmonMatch.setSalmonsNumber((myCoordinateType.equals("OSO"))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                    energyUsed = 2;
                }            
            }

            // Si me muevo a la izquierda, independientemente de donde esté.
            else if(newCoordinate.x().equals(myCoordinate.x()-1)) {
                if(osoBoolean(myTile, toTravel, myCoordinateType, toTravelType, List.of(2,3), List.of(0,5))) {
                    salmonMatch.setSalmonsNumber((myCoordinateType.equals(toTravelType))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
                    energyUsed = 2;
                }
                else if(saltoBoolean(myTile, toTravel, myCoordinateType, toTravelType, List.of(1,2,3), List.of(0,4,5))) {
                    salmonMatch.setSalmonsNumber((myCoordinateType.equals("OSO"))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                    energyUsed = 2;
                }
            }
        }

        else throw new NotValidMoveException("Solo puedes moverte de uno en uno"); 
        if (newCoordinate2 == null) {
            salmonMatch.setCoordinate(newCoordinate); 
            toTravel.setSalmonsNumber(toTravel.getSalmonsNumber()+1);
            matchTileService.save(toTravel);
        }
        else if (toTravel2 != null) { 
            salmonMatch.setCoordinate(newCoordinate2); 
            toTravel2.setSalmonsNumber(toTravel2.getSalmonsNumber()+1);
            matchTileService.save(toTravel2);
        }
        player.setEnergy(player.getEnergy() - energyUsed);   
        if(player.getEnergy() == 0) {
            changePlayer(player, match, players, numPlayers);
        }
        playerService.savePlayer(player);
        if (salmonMatch.getSalmonsNumber() > 0) salmonMatchService.save(salmonMatch);
        else salmonMatchService.delete(salmonMatch.getId());
        return new ResponseEntity<>(salmonMatch, HttpStatus.OK);
    }

    @PostMapping("/player/{playerId}")
    public void create(@PathVariable("playerId") Integer playerId) {
        for (int i=0; i < 4; i++) {
            Player player = playerService.getById(playerId);
            Salmon salmon = salmonService.findAll().stream().filter(sal -> sal.getColor().equals(player.getColor())).findFirst().orElse(null);
            Match match = player.getMatch();
            Coordinate coordinate = new Coordinate(null, null);;
            SalmonMatch salmonMatch = new SalmonMatch();
            salmonMatch.setPlayer(player);
            salmonMatch.setSalmonsNumber(2);
            salmonMatch.setSpawningNumber(0);
            salmonMatch.setCoordinate(coordinate);
            salmonMatch.setSalmon(salmon);
            salmonMatch.setMatch(match);
            salmonMatchService.save(salmonMatch);
        }
    }

    @PatchMapping("/enterSpawn/{id}")
    public ResponseEntity<SalmonMatch> enterSpawn(@PathVariable Integer id) {
        SalmonMatch salmonMatch = salmonMatchService.getById(id);
        Player player = salmonMatch.getPlayer();
        Match match = salmonMatch.getMatch();
        List<Player> players = playerService.getPlayersByMatch(match.getId());
        Integer numPlayers = match.getPlayersNum();
        Coordinate myCoordinate = salmonMatch.getCoordinate();
        Coordinate newCoordinate = new Coordinate(1,5);
        Coordinate updateCoordinate = new Coordinate(1, 21);
        Integer energyUsed = 1;
        MatchTile myTile = matchTileService.findByMatchId(match.getId()).stream()
            .filter(m -> salmonMatch.getCoordinate().equals(m.getCoordinate())).findFirst().orElse(null);
        String tileType = myTile.getTile().getType().getType();

        if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
        // Para subir 
        if(newCoordinate.y() == myCoordinate.y() + 1 && myCoordinate.x().equals(newCoordinate.x())) { 
            if(tileType.equals("OSO") && List.of(3,4).contains(myTile.getOrientation())) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
            energyUsed = 2;    
        }
    
        else if(tileType.equals("SALTO") && List.of(2,3,4).contains(myTile.getOrientation())) {
            energyUsed = 2;    
        }
    }
        // Si vengo desde la izquierda
        else if(newCoordinate.x().equals(myCoordinate.x() + 1)) { 
            if(tileType.equals("OSO") && List.of(4,5).contains(myTile.getOrientation())) {
                    salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                    energyUsed = 2;    
            }
            else if(tileType.equals("SALTO") && List.of(3,4,5).contains(myTile.getOrientation())) {
                energyUsed = 2;    
            }
        }

        // Vengo de la derecha
        else if(newCoordinate.x().equals(myCoordinate.x() - 1)){
            if(tileType.equals("OSO") && List.of(2,3).contains(myTile.getOrientation())) {
                salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                energyUsed = 2;    
        }
            else if(tileType.equals("SALTO") && List.of(1,2,3).contains(myTile.getOrientation())) {
                energyUsed = 2;    
            }

        }

        salmonMatch.setCoordinate(updateCoordinate);
        player.setEnergy(player.getEnergy() - energyUsed);

    }

    
    if(player.getEnergy() == 0) {
        Integer myOrder = player.getPlayerOrder();
        Player nextPlayer = players.stream().filter(pl -> pl.getPlayerOrder().equals((myOrder + 1)%numPlayers)).findFirst().orElse(null);
        match.setActualPlayer(nextPlayer);
        matchService.save(match);
    }
    
    playerService.savePlayer(player);
    if (salmonMatch.getSalmonsNumber() > 0) salmonMatchService.save(salmonMatch);
    else salmonMatchService.delete(salmonMatch.getId());
    return new ResponseEntity<>(salmonMatch, HttpStatus.OK);

    }

}

    

