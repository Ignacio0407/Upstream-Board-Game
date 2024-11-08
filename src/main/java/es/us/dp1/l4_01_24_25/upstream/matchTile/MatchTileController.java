package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MatchTile> createTile(@RequestBody @Valid MatchTile matchTile) throws DataAccessException{
        return new ResponseEntity<>(matchTileService.save(matchTile), HttpStatus.OK);
    }
    
}
