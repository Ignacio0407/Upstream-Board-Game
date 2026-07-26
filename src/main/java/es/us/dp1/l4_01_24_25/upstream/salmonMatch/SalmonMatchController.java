package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.model.BaseRestControllerWithDTO;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatchService.MoveRequest;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SalmonMatchDTO;

@RestController
@RequestMapping("/api/v1/salmonMatches")
public class SalmonMatchController extends BaseRestControllerWithDTO<SalmonMatch, SalmonMatchDTO, Integer>{

    SalmonMatchService salmonMatchService;

    @Autowired
    public SalmonMatchController(SalmonMatchService salmonMatchService) {
        super(salmonMatchService);
        this.salmonMatchService = salmonMatchService;
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<SalmonMatchDTO>> findAllFromMatch(@PathVariable Integer matchId) {  
        return new ResponseEntity<>(this.salmonMatchService.findAllFromMatchDTO(matchId), HttpStatus.OK);
    }

    @PatchMapping("/move-batch/{id}")
    public ResponseEntity<Void> updateCoordinateBatch(@PathVariable Integer id, @RequestBody List<MoveRequest> pendingMoves) {
        this.salmonMatchService.updateCoordinateBatch(id, pendingMoves);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/player/{playerId}")
    public ResponseEntity<Void> create(@PathVariable("playerId") Integer playerId) {
        this.salmonMatchService.create(playerId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}