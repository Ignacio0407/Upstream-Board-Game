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

import es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MatchTileDTO;
import es.us.dp1.l4_01_24_25.upstream.model.BaseRestControllerWithDTO;

@RestController
@RequestMapping("/api/v1/matchTiles")
public class MatchTileController extends BaseRestControllerWithDTO<MatchTile, MatchTileDTO, Integer>{

    MatchTileService matchTileService;

    public MatchTileController(MatchTileService matchTileService) {
        super(matchTileService);
        this.matchTileService = matchTileService;
    }

    @GetMapping("match/{id}")
    public ResponseEntity<List<MatchTileDTO>> findByMatchId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(this.matchTileService.findByMatchIdAsDTO(id));
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<MatchTileDTO> updateMatchTile(@PathVariable("id") Integer id, @RequestBody Map<String, Integer> updates) {
        return ResponseEntity.ok(this.matchTileService.updateCoordinate(id, updates));
    }

    @PatchMapping("/{id}/rotation")
    public ResponseEntity<MatchTileDTO> updateMatchTileRotation(@PathVariable("id") Integer id, @RequestBody Integer rotation) {
        return ResponseEntity.ok(this.matchTileService.updateMatchTileRotation(id, rotation));
    }

    @PostMapping("/createMatchTiles/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<MatchTileDTO>> createMultipleMatchTiles(@PathVariable("id") Integer id) throws DataAccessException {
        return new ResponseEntity<>(this.matchTileService.createMultipleMatchTiles(id), HttpStatus.CREATED);
    }

}