package es.us.dp1.l4_01_24_25.upstream.tile;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/tiles")
@Tag(name = "tile", description = "Tile API")
public class TileController {
 
    TileService tileService;

    @Autowired
    public TileController(TileService cs) {
        this.tileService = cs;
    }

    @GetMapping
    public ResponseEntity<List<Tile>> findAll() {
        List<Tile> res = (List<Tile>) tileService.findAll();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public Tile findById(int id) {
        Optional<Tile> c = tileService.findById(id);
        if(!c.isPresent()) {
            throw new ResourceNotFoundException("Casilla","id",id);
        }
        return c.get();
    }

}
