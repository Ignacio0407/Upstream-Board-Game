package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.general.BaseRestController;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileService;

@RestController
@RequestMapping("/api/v1/matchTiles")
public class MatchTileController extends BaseRestController<MatchTile,Integer>{

    MatchTileService matchTileService;
    TileService tileService;
    MatchService matchService;

    public MatchTileController(MatchTileService matchTileService, TileService tileService, MatchService matchService) {
        super(matchTileService);
        this.tileService = tileService;
        this.matchService = matchService;
    }

    @PatchMapping("/{id}")
    @SuppressWarnings({"BoxedValueEquality", "NumberEquality"})
    public ResponseEntity<MatchTile> updateMatchTile(@PathVariable("id") Integer id, 
                                                     @RequestBody Map<String, Integer> updates) throws ResourceNotFoundException {
        
        MatchTile matchTile = matchTileService.findById(id);
        
        if (matchTile == null) {
            throw new ResourceNotFoundException("MatchTile", "ID", id);
        }
        if(updates.get("y") != 0){
            MatchTile matchTile2 = matchTileService.findByMatchId(matchTile.getMatch().getId()).stream().filter(mT -> mT.getCoordinate().y() == updates.get("y")-1 
            && mT.getCoordinate().x() == updates.get("x")).findFirst().orElse(null);
            if(matchTile2 == null){
                throw new ResourceNotFoundException("No se puede actualizar el MatchTile en esta ronda", "ID",id);
        }
        }
    
        Boolean positionOccupied = matchTileService.findByMatchId(matchTile.getMatch().getId()).stream()
        .anyMatch(mT -> mT.getCoordinate() != null 
                     && mT.getCoordinate().x() == updates.get("x") 
                     && mT.getCoordinate().y() == updates.get("y"));

        if (positionOccupied) {
            throw new IllegalStateException("Ya existe una MatchTile en las coordenadas especificadas.");
        }
    
        if((matchTile.getMatch().getRound() == 0 && updates.get("y") > 3) || (matchTile.getMatch().getRound() == 1 && updates.get("y") > 4)
        || (matchTile.getMatch().getRound() == 6 && updates.get("x") == 1 && updates.get("y") == 5)){
            throw new ResourceNotFoundException("No se puede actualizar el MatchTile en esta ronda", "ID",id);
        }
             
        if (updates.containsKey("x") && updates.containsKey("y")) {
            Integer x = updates.get("x");
            Integer y = updates.get("y");
                 
            matchTile.setCoordinate(new Coordinate(x, y));
        }

        MatchTile updatedMatchTile = matchTileService.save(matchTile);
        return ResponseEntity.ok(updatedMatchTile);
    }


    @GetMapping("match/{id}")
    public List<MatchTile> findByMatchId(@PathVariable("id") Integer id) {
        return matchTileService.findByMatchId(id);
    }

    @PatchMapping("/{id}/rotation")
    public ResponseEntity<MatchTile> updateMatchTileRotation(@PathVariable("id") Integer id, @RequestBody Integer rotation) 
    throws ResourceNotFoundException {

        MatchTile matchTile = matchTileService.findById(id);
        if (matchTile == null) {
            throw new ResourceNotFoundException("MatchTile", "ID", id);
        }

        matchTile.setOrientation(rotation);
        //matchTile.setJumpingSides(matchTile.getJumpingSides().stream().map(side -> side+1).collect(Collectors.toList()));

    MatchTile updatedMatchTile = matchTileService.save(matchTile);

    return ResponseEntity.ok(updatedMatchTile);
}

    @PostMapping("/createMatchTiles/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<MatchTile>> createMultipleMatchTiles(@PathVariable("id") Integer id) throws DataAccessException {
        Tile water = tileService.findById(1);
        Tile rock = tileService.findById(2);
        Tile garza = tileService.findById(3);
        Tile bear = tileService.findById(4);
        Tile eagle = tileService.findById(5);
        Tile jump = tileService.findById(6);
        if (water == null || rock == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Match match = matchService.findById(id);
        if (match == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<MatchTile> createdTiles = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            MatchTile matchTile = new MatchTile();
            matchTile.setTile(water);
            matchTile.setMatch(match);
            matchTile.setCapacity(match.getPlayersNumber());
            matchTile.setOrientation(0);
            matchTile.setCoordinate(null);
            matchTile.setSalmonsNumber(0);
            createdTiles.add(matchTile);
        }
        for (int i = 0; i < 5; i++) {
            MatchTile matchTile = new MatchTile();
            matchTile.setTile(rock);
            matchTile.setMatch(match);
            if (match.getPlayersNumber() > 2) matchTile.setCapacity(match.getPlayersNumber()-1);
            else matchTile.setCapacity(2);
            matchTile.setOrientation(0);
            matchTile.setCoordinate(null);
            matchTile.setSalmonsNumber(0);
            createdTiles.add(matchTile);
        }
        for (int i = 0; i < 5; i++) {
            MatchTile matchTile = new MatchTile();
            matchTile.setTile(garza);
            matchTile.setMatch(match);
            matchTile.setCapacity(match.getPlayersNumber());
            matchTile.setOrientation(0);
            matchTile.setCoordinate(null);
            matchTile.setSalmonsNumber(0);
            createdTiles.add(matchTile);
        }
        for (int i = 0; i < 3; i++) {
            MatchTile matchTile = new MatchTile();
            matchTile.setTile(bear);
            matchTile.setMatch(match);
            matchTile.setCapacity(match.getPlayersNumber());
            matchTile.setOrientation(0);
            matchTile.setCoordinate(null);
            matchTile.setSalmonsNumber(0);
            createdTiles.add(matchTile);
        }
        for (int i = 0; i < 5; i++) {
            MatchTile matchTile = new MatchTile();
            matchTile.setTile(eagle);
            matchTile.setMatch(match);
            matchTile.setCapacity(match.getPlayersNumber());
            matchTile.setOrientation(0);
            matchTile.setCoordinate(null);
            matchTile.setSalmonsNumber(0);
            createdTiles.add(matchTile);
        } 
        for (int i = 0; i < 4; i++) {
            MatchTile matchTile = new MatchTile();
            matchTile.setTile(jump);
            matchTile.setMatch(match);
            matchTile.setCapacity(match.getPlayersNumber());
            matchTile.setOrientation(0);
            matchTile.setCoordinate(null);
            matchTile.setSalmonsNumber(0);
            createdTiles.add(matchTile);
        }    
        Collections.shuffle(createdTiles);
        createdTiles.stream().forEach(mT -> matchTileService.save(mT));

        return new ResponseEntity<>(createdTiles, HttpStatus.CREATED);
    }
  
    @GetMapping("/prueba1/{matchId}")
    public List<MatchTile> findMatchTileCoordinates(@PathVariable("matchId") Integer matchId) {
        return matchTileService.findByMatchIdNoCoord(matchId);
    }

}




