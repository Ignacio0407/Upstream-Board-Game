package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;
import java.util.Map;

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

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.model.BaseRestController;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerRestController extends BaseRestController<Player,Integer>{
    
    PlayerService playerService;
    UserService userService;
    MatchService matchService;

    public PlayerRestController(PlayerService playerService, UserService userService, MatchService matchService) {
        super(playerService);
        this.userService = userService;
        this.matchService = matchService;
    }

    @GetMapping("/match/{id}")
    public ResponseEntity<List<Player>> findPlayersByMatchId (@PathVariable("id")  Integer id) throws ResourceNotFoundException {
        return new ResponseEntity<>(playerService.findPlayersByMatch(id), HttpStatus.OK);
    }

    @PostMapping("/match/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Player> createPlayerInMatch(@PathVariable("id") Integer matchId, @RequestBody Map<String,String> requestBody) {
        return new ResponseEntity<>(playerService.createPlayerInMatch(matchId, requestBody), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/energy")
    public ResponseEntity<Player> updateEnergy(@PathVariable("id") Integer id, @RequestBody @Valid Integer energyUsed) throws ResourceNotFoundException, Exception {
        return new ResponseEntity<>(playerService.updateEnergy(id, energyUsed), HttpStatus.OK);
    }

    @PatchMapping("/{id}/regenerateEnergy")
    public ResponseEntity<Player> regenerateEnergy(@PathVariable("id") Integer id) throws ResourceNotFoundException, Exception {
        return new ResponseEntity<>(playerService.regenerateEnergy(id), HttpStatus.OK);
    }
       
}