package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;
import java.util.Map;

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

import es.us.dp1.l4_01_24_25.upstream.exceptions.InsufficientEnergyException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NoCapacityException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.OnlyMovingForwardException;
import es.us.dp1.l4_01_24_25.upstream.model.BaseRestControllerWithDTO;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SalmonMatchDTO;
import jakarta.validation.Valid;

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

    @PatchMapping("/coordinate/{id}")
    public ResponseEntity<SalmonMatchDTO> updateCoordinate(@PathVariable Integer id, @RequestBody @Valid Map<String,Integer> coordinate) throws NotValidMoveException,  InsufficientEnergyException, OnlyMovingForwardException, NoCapacityException {
        this.salmonMatchService.updateCoordinate(id, coordinate);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/player/{playerId}")
    public ResponseEntity<Void> create(@PathVariable("playerId") Integer playerId) {
        this.salmonMatchService.create(playerId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/enterSpawn/{id}")
    public ResponseEntity<SalmonMatchDTO> enterSpawn(@PathVariable Integer id) {
        this.salmonMatchService.enterSpawn(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}