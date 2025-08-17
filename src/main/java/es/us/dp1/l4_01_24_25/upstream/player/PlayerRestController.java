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
import es.us.dp1.l4_01_24_25.upstream.model.BaseRestControllerWithDTO;
import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO;
import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.LobbyPlayerDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerRestController extends BaseRestControllerWithDTO<Player, PlayerDTO, Integer>{
    
    PlayerService playerService;

    public PlayerRestController(PlayerService playerService) {
        super(playerService);
        this.playerService = playerService;
    }

    @GetMapping("/match/{id}")
    public ResponseEntity<List<PlayerDTO>> findPlayersByMatchId (@PathVariable("id")  Integer id) throws ResourceNotFoundException {
        return new ResponseEntity<>(this.playerService.findPlayersByMatchAsDTO(id), HttpStatus.OK);
    }

    @GetMapping("/match/sorted/{id}")
    public ResponseEntity<List<PlayerDTO>> findPlayersByMatchIdSortedPlayerOrder (@PathVariable("id")  Integer id) throws ResourceNotFoundException {
        return new ResponseEntity<>(this.playerService.findPlayersByMatchSortedPlayerOrderAsDTO(id), HttpStatus.OK);
    }

    @PostMapping("/match/{id}")
    public ResponseEntity<LobbyPlayerDTO> createPlayerInMatch(@PathVariable("id") Integer matchId, @RequestBody Map<String,String> playerdata) {
        return new ResponseEntity<>(this.playerService.createPlayerInMatch(matchId, playerdata), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/energy")
    public ResponseEntity<PlayerDTO> updateEnergy(@PathVariable("id") Integer id, @RequestBody @Valid Integer energyUsed) throws ResourceNotFoundException, Exception {
        return new ResponseEntity<>(this.playerService.updateEnergy(id, energyUsed), HttpStatus.OK);
    }
       
}