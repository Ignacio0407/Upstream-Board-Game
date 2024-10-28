package es.us.dp1.l4_01_24_25.upstream.partidas;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/match")
@Tag(name = "match", description = "Match API")
public class PartidaController {
    
    PartidaService ps;

    @Autowired
    public PartidaController(PartidaService ps) {
        this.ps = ps;
    }

    @GetMapping
    public ResponseEntity<List<Partida>> findAll() {
        List<Partida> res = (List<Partida>) ps.findAll();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
