package upstream.player;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import upstream.exceptions.ResourceNotFoundException;
import upstream.model.BaseRestControllerWithDTO;
import upstream.player.playerdto.PlayerDTO;
import upstream.player.playerdto.LobbyPlayerDTO;
import upstream.player.playerdto.CreatePlayerRequest;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerRestController extends BaseRestControllerWithDTO<Player, PlayerDTO, Integer>{
    
    PlayerService playerService;

    public PlayerRestController(PlayerService playerService) {
        super(playerService);
        this.playerService = playerService;
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        this.playerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/match/{id}")
    public ResponseEntity<List<PlayerDTO>> findPlayersByMatchId (@PathVariable("id")  Integer id) throws ResourceNotFoundException {
        return new ResponseEntity<>(this.playerService.findPlayersByMatchAsDTO(id), HttpStatus.OK);
    }

    @PostMapping("/match/{id}")
    public ResponseEntity<LobbyPlayerDTO> createPlayerInMatch(
            @PathVariable("id") Integer matchId, 
            @Valid @RequestBody CreatePlayerRequest request) {
        return new ResponseEntity<>(this.playerService.createPlayerInMatch(matchId, request), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/energy")
    public ResponseEntity<PlayerDTO> setEnergyToZero(@PathVariable("id") Integer id) {
        this.playerService.setEnergyToZero(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
 
}