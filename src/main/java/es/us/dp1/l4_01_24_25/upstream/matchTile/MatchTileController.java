package es.us.dp1.l4_01_24_25.upstream.matchTile;

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

import es.us.dp1.l4_01_24_25.upstream.model.BaseRestController;

@RestController
@RequestMapping("/api/v1/matchTiles")
public class MatchTileController extends BaseRestController<MatchTile,Integer>{

    MatchTileService matchTileService;

    public MatchTileController(MatchTileService matchTileService) {
        super(matchTileService);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MatchTile> updateMatchTile(@PathVariable("id") Integer id, @RequestBody Map<String, Integer> updates) {
        return ResponseEntity.ok(matchTileService.updateCoordinate(id, updates));
    }

    @GetMapping("match/{id}")
    public ResponseEntity<List<MatchTile>> findByMatchId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(matchTileService.findByMatchId(id));
    }

    @PatchMapping("/{id}/rotation")
    public ResponseEntity<MatchTile> updateMatchTileRotation(@PathVariable("id") Integer id, @RequestBody Integer rotation) {
        return ResponseEntity.ok(matchTileService.updateMatchTileRotation(id, rotation));
    }

    @PostMapping("/createMatchTiles/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<MatchTile>> createMultipleMatchTiles(@PathVariable("id") Integer id) throws DataAccessException {
        return new ResponseEntity<>(matchTileService.createMultipleMatchTiles(id), HttpStatus.CREATED);
    }
  
    @GetMapping("/prueba1/{matchId}")
    public List<MatchTile> findMatchTileCoordinates(@PathVariable("matchId") Integer matchId) {
        return matchTileService.findByMatchIdNoCoord(matchId);
    }

}