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
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.model.BaseRestControllerWithDTO;
import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO;
import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.LobbyPlayerDTO;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerRestController extends BaseRestControllerWithDTO<Player, PlayerDTO, Integer>{
    
    PlayerService playerService;
    UserService userService;
    MatchService matchService;

    public PlayerRestController(PlayerService playerService, UserService userService, MatchService matchService) {
        super(playerService);
        this.playerService = playerService;
        this.userService = userService;
        this.matchService = matchService;
    }

    @GetMapping("/match/{id}")
    public ResponseEntity<List<PlayerDTO>> findPlayersByMatchId (@PathVariable("id")  Integer id) throws ResourceNotFoundException {
        return new ResponseEntity<>(playerService.findPlayersByMatchAsDTO(id), HttpStatus.OK);
    }

    @PostMapping("/match/{id}")
    public ResponseEntity<LobbyPlayerDTO> createPlayerInMatch(@PathVariable("id") Integer matchId, @RequestBody Map<String,String> playerdata) {
        return new ResponseEntity<>(playerService.createPlayerInMatch(matchId, playerdata), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/energy")
    public ResponseEntity<PlayerDTO> updateEnergy(@PathVariable("id") Integer id, @RequestBody @Valid Integer energyUsed) throws ResourceNotFoundException, Exception {
        return new ResponseEntity<>(playerService.updateEnergy(id, energyUsed), HttpStatus.OK);
    }

    // Possible to delete
    @PatchMapping("/{id}/regenerateEnergy")
    public ResponseEntity<PlayerDTO> regenerateEnergy(@PathVariable("id") Integer id) throws ResourceNotFoundException, Exception {
        return new ResponseEntity<>(playerService.regenerateEnergy(id), HttpStatus.OK);
    }
       
}