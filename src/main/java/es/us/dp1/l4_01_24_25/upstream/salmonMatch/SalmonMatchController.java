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

    @SuppressWarnings("unused")
    private void throwExceptions( MatchTile toTravel, Integer salmonsInToTravel, Coordinate myCoordinate, Coordinate newCoordinate, Player player) {
        // if (toTravel.getCapacity() < salmonsInToTravel + 1) throw new NoCapacityException("No queda hueco en esta casilla");
 
         if(myCoordinate != null && myCoordinate.equals(newCoordinate)) throw new NotValidMoveException("No puedes moverte en la misma casilla");
 
         if (player.getEnergy() <= 0) throw new InsufficientEnergyException("No hay suficiente energía para ese movimiento");
 
         if (myCoordinate == null && newCoordinate.y() != 0) throw new NotValidMoveException("Solo puedes moverte de uno en uno"); 
 
    }

    private MatchTile tileFullNull(SalmonMatch salmonMatch, List<MatchTile> matchTiles, Coordinate newCoordinate) {
        Coordinate newCoordinate2 = new Coordinate(newCoordinate.x(), newCoordinate.y()+1);
        MatchTile toTravel2 = matchTiles.stream().filter(mT -> mT.getCoordinate() != null && mT.getCoordinate().equals(newCoordinate2)).toList().get(0);
        String toTravelType2 = toTravel2.getTile().getType().getType();
        if(toTravelType2.equals("OSO") && List.of(0, 1).contains(toTravel2.getOrientation())) 
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber() - 1);
        return toTravel2;
    }
    
    private MatchTile handleTileFull(Coordinate newCoordinate, SalmonMatch salmonMatch, MatchTile myTile, 
        String myCoordinateType, List<MatchTile> matchTiles, List<Integer> from, List<Integer> to, Integer x, Integer y) {
        Coordinate newCoordinate2 = newCoordinateToTravel(newCoordinate, x, y);
        MatchTile toTravel2 = matchTiles.stream().filter(mT -> mT.getCoordinate() != null && mT.getCoordinate().equals(newCoordinate2)).findFirst().orElse(null);
        if (toTravel2 == null) throw new NotValidMoveException("¡No se puede ir a la casilla a saltar o todavía no está puesta!");
        String toTravelType2 = toTravel2.getTile().getType().getType();
        if (osoBoolean(myTile, toTravel2, myCoordinateType, toTravelType2, from, to)) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber() - 1);
        }
        return toTravel2;
    }

    private Coordinate newCoordinateToTravel(Coordinate newCoordinate, Integer x, Integer y) {
        return new Coordinate(newCoordinate.x()+x, newCoordinate.y() + y);
    }

    private void changePlayer(Player player, Match match, List<Player> players, Integer numPlayers) {
        Integer myOrder = player.getPlayerOrder();
        List<SalmonMatch> salmonMatchesFromPlayer = salmonMatchService.getAllFromMatch(player.getId());
        List<MatchTile> herons = matchService.getHeronWithCoordsFromGame(match.getId());
        for(MatchTile h : herons) {
            for(SalmonMatch s: salmonMatchesFromPlayer){
                if(s.getCoordinate().equals(h.getCoordinate())){
                    s.setSalmonsNumber(s.getSalmonsNumber()-1);
                    if(s.getSalmonsNumber()==0) {
                        salmonMatchService.delete(s.getId()); 
                        h.setSalmonsNumber(h.getSalmonsNumber()-1);
                        matchTileService.save(h);
                    }
                    else salmonMatchService.save(s);
                }
            }   
        }
        playerService.savePlayer(player);
        matchService.save(match);
        Player nextPlayer = players.stream().filter(pl -> pl.getPlayerOrder().equals((myOrder + 1)%numPlayers)).findFirst().orElse(null);
        match.setActualPlayer(nextPlayer);
        matchService.save(match);
    }

    private ResponseEntity<SalmonMatch> processSalmonMovement(SalmonMatch salmonMatch, MatchTile toTravel, Player player, Match match, 
            Coordinate newCoordinate, Integer energyUsed, List<Player> players, Integer numPlayers) {
        salmonMatch.setCoordinate(newCoordinate);
        toTravel.setSalmonsNumber(toTravel.getSalmonsNumber() + 1);
        matchTileService.save(toTravel);
        player.setEnergy(player.getEnergy() - energyUsed);
        if (player.getEnergy() == 0) {
            changePlayer(player, match, players, numPlayers);
        }
        playerService.savePlayer(player);
        if (salmonMatch.getSalmonsNumber() > 0) {
            salmonMatchService.save(salmonMatch);
        } else {
            salmonMatchService.delete(salmonMatch.getId());
        }
        return new ResponseEntity<>(salmonMatch, HttpStatus.OK);
    }

    private Boolean osoBoolean(MatchTile myTile, MatchTile toTravel, String myCoordinateType, String toTravelType, List<Integer> from, List<Integer> to) {
        return myCoordinateType.equals("OSO") && from.contains(myTile.getOrientation()) ||
            toTravelType.equals("OSO") && to.contains(toTravel.getOrientation());
    }

    /*private Boolean saltoBoolean(MatchTile myTile, MatchTile toTravel, String myCoordinateType, String toTravelType, List<Integer> from, List<Integer> to) {
        return myCoordinateType.equals("SALTO") && from.contains(myTile.getOrientation()) ||
            toTravelType.equals("SALTO") && to.contains(toTravel.getOrientation());
    } */

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
                toTravel2 = tileFullNull(salmonMatch, matchTiles, newCoordinate);
                if (!toTravel2.getCapacity().equals(toTravel2.getSalmonsNumber())) {
                    energyUsed = 3;
                    newCoordinate2 = new Coordinate(newCoordinate.x(), newCoordinate.y()+1);
                    if (toTravel2.getTile().getType().getType().equals("AGUILA")) {
                        salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                        toTravel2 = matchTileService.eagleToWater(toTravel2, match);
                        if (salmonMatch.getSalmonsNumber().equals(0)) 
                            salmonMatchService.delete(salmonMatch.getId());
                    }
                }
                else throw new NotValidMoveException("¡La casilla siguiente también está llena!");
            }
            else if(toTravelType.equals("OSO") && List.of(0, 1).contains(toTravel.getOrientation())) {
                salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                energyUsed = 2;
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

            if(distancia.y() < 0) throw new NotValidMoveException("Solo puedes moverte hacia delante"); 

            if(myCoordinate.y().equals(newCoordinate.y()) && myCoordinate.x().equals(1)) throw new NotValidMoveException("Solo puedes moverte hacia delante");

            if(List.of(0, 2).contains(myCoordinate.x()) && Math.abs(distancia.x()) == 1 && Math.abs(distancia.y()) == 1) throw new NotValidMoveException("Este movimiento no está permitido");    

            if (toTravel.getCapacity().equals(toTravel.getSalmonsNumber())) {
                if (toTravel.getCapacity().equals(toTravel.getSalmonsNumber())) {
                    if (null != myCoordinate.x()) {
                        // Si estoy en el centro
                        if (newCoordinate.x().equals(myCoordinate.x())) { // Si subo hacia arriba
                            toTravel2 = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType, matchTiles, List.of(3, 4), List.of(0, 1), 0, 1);
                            if (!toTravel2.getCapacity().equals(toTravel2.getSalmonsNumber())) {
                                energyUsed = 3;
                                newCoordinate2 = newCoordinateToTravel(newCoordinate, 0, 1);
                            }
                            else throw new NotValidMoveException("¡La casilla adyacente y la siguiente están llenas!");
                        }
                        
                        // Si voy al centro desde la izquierda
                        else if (newCoordinate.x() == myCoordinate.x() + 1) {
                            toTravel2 = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType, matchTiles, List.of(4, 5), List.of(1, 2), 1, 1);
                            if (!toTravel2.getCapacity().equals(toTravel2.getSalmonsNumber())) {
                                energyUsed = 3;
                                newCoordinate2 = newCoordinateToTravel(newCoordinate, 1, 1);
                            }
                            else throw new NotValidMoveException("¡La casilla adyacente y la siguiente están llenas!");
                        }
                        
                        // Si voy al centro desde la derecha
                        else if (newCoordinate.x() == myCoordinate.x() - 1) {
                            toTravel2 = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType, matchTiles, List.of(2, 3), List.of(0, 5), -1, 1);
                            if (!toTravel2.getCapacity().equals(toTravel2.getSalmonsNumber())) {
                                energyUsed = 3;
                                newCoordinate2 = newCoordinateToTravel(newCoordinate, -1, 1);
                            }
                            else throw new NotValidMoveException("¡La casilla adyacente y la siguiente están llenas!");
                        }
                    else throw new NotValidMoveException("¡La casilla adyacente está llena y no se puede saltar a otra!");
                if (newCoordinate2 != null) {
                    if (player.getEnergy() < 3) 
                        throw new NotValidMoveException("¡Necesitas 3 puntos de energía para saltar una casilla llena!");
                    if (toTravel2.getTile().getType().getType().equals("AGUILA")) {
                        salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                        toTravel2 = matchTileService.eagleToWater(toTravel2, match);
                        if (salmonMatch.getSalmonsNumber().equals(0)) 
                            salmonMatchService.delete(salmonMatch.getId());
                    }
                    myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
                    matchTileService.save(myTile);
                    return processSalmonMovement(salmonMatch, toTravel2, player, match, newCoordinate2, energyUsed, players, numPlayers);
                }
            }
        }
    }

            if(toTravelType.equals("AGUILA")) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
            toTravel = matchTileService.eagleToWater(toTravel, match);
        } 
            // SI SUBO
            if(newCoordinate.x().equals(myCoordinate.x()) && newCoordinate.y().equals(myCoordinate.y()+1)) {
                Boolean cond1 = toTravelType.equals("OSO") && List.of(0,1).contains(toTravel.getOrientation());
                Boolean cond2 = toTravelType.equals("SALTO") && List.of(0,1,5).contains(toTravel.getOrientation());
                if(myCoordinateType.equals("OSO")) {
                    if(List.of(3,4).contains(myTile.getOrientation())) {
                        energyUsed = 2;
                        salmonMatch.setSalmonsNumber((toTravelType.equals("OSO"))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
                    }
                    else {
                        if(cond1 || cond2) energyUsed = 2;
                        salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-2:cond2?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                    }
                }
                else if(myCoordinateType.equals("SALTO")) {
                    if(List.of(2,3,4).contains(myTile.getOrientation())) {
                        energyUsed = 2;
                        salmonMatch.setSalmonsNumber((toTravelType.equals("OSO"))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                    }
                    else {
                        if(cond1 || cond2) energyUsed = 2;
                        salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                    }
                }
                else {
                    if(cond1 || cond2) energyUsed = 2;
                    salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                }
            }

            // SI VOY A LA IZQUIERDA
            if(newCoordinate.x().equals(myCoordinate.x()-1)) {
                Boolean cond1 = toTravelType.equals("OSO") && List.of(0,5).contains(toTravel.getOrientation());
                Boolean cond2 = toTravelType.equals("SALTO") && List.of(0,4,5).contains(toTravel.getOrientation());
                if(myCoordinateType.equals("OSO")) {
                    if(List.of(2,3).contains(myTile.getOrientation())) {
                        energyUsed = 2;
                        salmonMatch.setSalmonsNumber((toTravelType.equals("OSO"))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
                    }
                    else {
                        if(cond1 || cond2) energyUsed = 2;
                        salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-2:cond2?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber()); 
                    }
                }
                else if(myCoordinateType.equals("SALTO")) {
                    if(List.of(1,2,3).contains(myTile.getOrientation())) {
                        energyUsed = 2;
                        salmonMatch.setSalmonsNumber((toTravelType.equals("OSO"))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                    }
                    else {
                        if(cond1 || cond2) energyUsed = 2;
                        salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                    }
                }
                else {
                    if(cond1 || cond2) energyUsed = 2;
                    salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                }
            }

            // SI VOY A LA DERECHA
            if(newCoordinate.x().equals(myCoordinate.x()+1)) {
                Boolean cond1 = toTravelType.equals("OSO") && List.of(1,2).contains(toTravel.getOrientation());
                Boolean cond2 = toTravelType.equals("SALTO") && List.of(0,1,2).contains(toTravel.getOrientation());
                if(myCoordinateType.equals("OSO")) {
                    if(List.of(4,5).contains(myTile.getOrientation())) {
                        energyUsed = 2;
                        salmonMatch.setSalmonsNumber((toTravelType.equals("OSO"))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
                    }
                    else {
                        if(cond1 || cond2) energyUsed = 2;
                        salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-2:cond2?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber()); 
                    }
                }
                else if(myCoordinateType.equals("SALTO")) {
                    if(List.of(3,4,5).contains(myTile.getOrientation())) {
                        energyUsed = 2;
                        salmonMatch.setSalmonsNumber((toTravelType.equals("OSO"))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                    }
                    else {
                        if(cond1 || cond2) energyUsed = 2;
                        salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                    }
                }
                else {
                    if(cond1 || cond2) energyUsed = 2;
                    salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                }
            }
            myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
            matchTileService.save(myTile);
        }

        else throw new NotValidMoveException("Solo puedes moverte de uno en uno"); 
        if (newCoordinate2 == null ) return processSalmonMovement(salmonMatch, toTravel, player, match, newCoordinate, energyUsed, players, numPlayers);
        else return processSalmonMovement(salmonMatch, toTravel2, player, match, newCoordinate2, energyUsed, players, numPlayers);
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
        myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
        matchTileService.save(myTile);
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