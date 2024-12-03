package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/salmonMatches")
@SecurityRequirement(name = "bearerAuth")
public class SalmonMatchController {

    private final salmonMatchService salmonMatchService;

    @Autowired
    public SalmonMatchController(salmonMatchService salmonMatchService){
        this.salmonMatchService = salmonMatchService;
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<SalmonMatch>> findAllFromMatch(@PathVariable Integer matchId) {  
        return new ResponseEntity<>(salmonMatchService.getAllFromMatch(matchId), HttpStatus.OK);
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<SalmonMatch>> findAllFromPlayer(@PathVariable Integer playerId) {  
        return new ResponseEntity<>(salmonMatchService.getAllFromPlayer(playerId), HttpStatus.OK);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<SalmonMatch> findById(@PathVariable("id") Integer id){
        return new ResponseEntity<>(salmonMatchService.getPartidaSalmon(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<SalmonMatch> create(@RequestBody @Valid SalmonMatch salmonMatch) {
        return new ResponseEntity<>(salmonMatchService.savePartidaSalmon(salmonMatch), HttpStatus.CREATED);
    }

}
