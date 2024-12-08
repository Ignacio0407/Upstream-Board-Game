package es.us.dp1.l4_01_24_25.upstream.matchTile;

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
import es.us.dp1.l4_01_24_25.upstream.util.RestPreconditions;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/matchTiles")
public class MatchTileController {

    private final MatchTileService matchTileService;

    public MatchTileController(MatchTileService matchTileService) {
        this.matchTileService = matchTileService;
    }

    @GetMapping
    public List<MatchTile> getAllMatchTiles() {
        return matchTileService.findAll();
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
    public ResponseEntity<MatchTile> updateMatchTileRotation(@PathVariable("id") Integer id, @RequestBody Integer rotation) 
    throws ResourceNotFoundException {

        MatchTile matchTile = matchTileService.findById(id);
        if (matchTile == null) {
            throw new ResourceNotFoundException("MatchTile", "ID", id);
        }

        matchTile.setOrientation(rotation);

        return ResponseEntity.ok(matchTileService.save(matchTile));
    }

    
}




