package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.auth.payload.response.MessageResponse;
import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileService;
import es.us.dp1.l4_01_24_25.upstream.util.RestPreconditions;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/matchTiles")
public class MatchTileController {

    private final MatchTileService matchTileService;
    private final TileService tileService;
    private final MatchService matchService;

    public MatchTileController(MatchTileService matchTileService, TileService tileService, MatchService matchService) {
        this.matchTileService = matchTileService;
        this.tileService = tileService;
        this.matchService = matchService;
    }

    @GetMapping
    public List<MatchTile> getAllMatchTiles() {
        return matchTileService.findAll();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MatchTile> updateMatchTile(@PathVariable("id") Integer id, 
                                                     @RequestBody Map<String, Integer> updates) throws ResourceNotFoundException {
        
        MatchTile matchTile = matchTileService.findById(id);
        
        if (matchTile == null) {
            throw new ResourceNotFoundException("MatchTile", "ID", id);
        }
        if(updates.get("y") != 0){
            MatchTile matchTile2 = matchTileService.findAll().stream().filter(mT -> mT.getCoordinate().y() == updates.get("y")-1 
            && mT.getCoordinate().x() == updates.get("x")).findFirst().orElse(null);
            if(matchTile2 == null){
                throw new ResourceNotFoundException("No se puede actualizar el MatchTile en esta ronda", "ID",id);
        }
        }
    
        boolean positionOccupied = matchTileService.findAll().stream()
        .anyMatch(mT -> mT.getCoordinate() != null 
                     && mT.getCoordinate().x() == updates.get("x") 
                     && mT.getCoordinate().y() == updates.get("y"));

    if (positionOccupied) {
        throw new IllegalStateException("Ya existe una MatchTile en las coordenadas especificadas.");
    }
    
        if(matchTile.getMatch().getRound() == 0 && updates.get("y") > 3){
            throw new ResourceNotFoundException("No se puede actualizar el MatchTile en esta ronda", "ID",id);
        }
             

        // Actualizar solo los valores de x e y si están presentes en el RequestBody
        if (updates.containsKey("x") && updates.containsKey("y")) {
            Integer x = updates.get("x");
            Integer y = updates.get("y");
                 
            matchTile.setCoordinate(new Coordinate(x, y));
        }

        MatchTile updatedMatchTile = matchTileService.save(matchTile);
        return ResponseEntity.ok(updatedMatchTile);
    }


    @GetMapping("/{id}")
    public List<MatchTile> getMatchTileById(@PathVariable("id") Integer id) {
        return matchTileService.findByMatchId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MatchTile> createTile(@RequestBody @Valid MatchTile matchTile) throws DataAccessException{
        return new ResponseEntity<>(matchTileService.save(matchTile), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageResponse> delete(@PathVariable("id") Integer id) {
        RestPreconditions.checkNotNull(matchTileService.findById(id), "MatchTile", "ID", id);
        matchTileService.deleteMatchTile(id);
        return new ResponseEntity<>(new MessageResponse("MatchTile deleted!"), HttpStatus.OK);    
    }

    @PatchMapping("/{id}/rotation")
public ResponseEntity<MatchTile> updateMatchTileRotation(
    @PathVariable("id") Integer id, 
    @RequestBody Integer rotation) throws ResourceNotFoundException {

    // Buscar el MatchTile por su ID
    MatchTile matchTile = matchTileService.findById(id);
    if (matchTile == null) {
        throw new ResourceNotFoundException("MatchTile", "ID", id);
    }

    // Actualizar el campo "orientation" (rotation)
    matchTile.setOrientation(rotation);

    // Guardar el MatchTile actualizado
    MatchTile updatedMatchTile = matchTileService.save(matchTile);

    // Retornar el MatchTile actualizado
    return ResponseEntity.ok(updatedMatchTile);
}

@PostMapping("/createMatchTiles/{id}")
@ResponseStatus(HttpStatus.CREATED)
public ResponseEntity<List<MatchTile>> createMultipleMatchTiles(@PathVariable("id") Integer id) throws DataAccessException {
    // Retrieve or create Tile objects for tile types 1 and 2
    Tile tileType1 = tileService.findById(1).orElse(null); // Assumes tileService has a method to find by type
    Tile tileType2 = tileService.findById(2).orElse(null);
    Tile tileType3 = tileService.findById(3).orElse(null); // Assumes tileService has a method to find by type
    Tile tileType4 = tileService.findById(4).orElse(null);
    Tile tileType5 = tileService.findById(5).orElse(null); // Assumes tileService has a method to find by type
    Tile tileType6 = tileService.findById(6).orElse(null);

    if (tileType1 == null || tileType2 == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Retrieve the Match object by ID
    Match match = matchService.getById(id);
    if (match == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    List<MatchTile> createdTiles = new ArrayList<>();

    // Create 7 tiles of type 1
    for (int i = 0; i < 7; i++) {
        MatchTile matchTile = new MatchTile();
        matchTile.setTile(tileType1);
        matchTile.setMatch(match);
        matchTile.setCapacity(match.getPlayersNum()); // Example capacity
        matchTile.setOrientation(0); // Example orientation
        matchTile.setCoordinate(null); // Example coordinate logic
        createdTiles.add(matchTile);
    }

    // Create 5 tiles of type 2
    for (int i = 0; i < 5; i++) {
        MatchTile matchTile = new MatchTile();
        matchTile.setTile(tileType2);
        matchTile.setMatch(match);
        matchTile.setCapacity(match.getPlayersNum()-1); // Example capacity
        matchTile.setOrientation(0); // Example orientation
        matchTile.setCoordinate(null); // Example coordinate logic
        createdTiles.add(matchTile);
    }
    for (int i = 0; i < 5; i++) {
        MatchTile matchTile = new MatchTile();
        matchTile.setTile(tileType3);
        matchTile.setMatch(match);
        matchTile.setCapacity(match.getPlayersNum()); // Example capacity
        matchTile.setOrientation(0); // Example orientation
        matchTile.setCoordinate(null); // Example coordinate logic
        createdTiles.add(matchTile);
    }
    for (int i = 0; i < 3; i++) {
        MatchTile matchTile = new MatchTile();
        matchTile.setTile(tileType4);
        matchTile.setMatch(match);
        matchTile.setCapacity(match.getPlayersNum()); // Example capacity
        matchTile.setOrientation(0); // Example orientation
        matchTile.setCoordinate(null); // Example coordinate logic
        createdTiles.add(matchTile);
    }

    for (int i = 0; i < 5; i++) {
        MatchTile matchTile = new MatchTile();
        matchTile.setTile(tileType5);
        matchTile.setMatch(match);
        matchTile.setCapacity(match.getPlayersNum()); // Example capacity
        matchTile.setOrientation(0); // Example orientation
        matchTile.setCoordinate(null); // Example coordinate logic
        createdTiles.add(matchTile);
    }

    for (int i = 0; i < 4; i++) {
        MatchTile matchTile = new MatchTile();
        matchTile.setTile(tileType6);
        matchTile.setMatch(match);
        matchTile.setCapacity(match.getPlayersNum()); // Example capacity
        matchTile.setOrientation(0); // Example orientation
        matchTile.setCoordinate(null); // Example coordinate logic
        createdTiles.add(matchTile);
    }
    Collections.shuffle(createdTiles);
    createdTiles.stream().forEach(mT -> matchTileService.save(mT));
    

    return new ResponseEntity<>(createdTiles, HttpStatus.CREATED);
}


    
}




