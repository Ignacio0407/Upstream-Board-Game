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
    public ResponseEntity<MatchTile> updateMatchTile(@PathVariable("id") Integer id, 
                                                     @RequestBody Map<String, Integer> updates) throws ResourceNotFoundException {
        
        MatchTile matchTile = matchTileService.findById(id);
        if (matchTile == null) {
            throw new ResourceNotFoundException("MatchTile", "ID", id);
        }

        if (updates.containsKey("x") && updates.containsKey("y")) {
            Integer x = updates.get("x");
            Integer y = updates.get("y");
            if (!matchTileService.validateTilePlacement(updates.get("round"), y)) {
                return ResponseEntity.badRequest().build();
            }
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
    
}
