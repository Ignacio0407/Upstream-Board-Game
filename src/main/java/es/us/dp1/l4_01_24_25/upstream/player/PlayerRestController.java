package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.general.BaseRestController;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import es.us.dp1.l4_01_24_25.upstream.util.RestPreconditions;
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
    public ResponseEntity<Player> createPlayerInMatch(@PathVariable("id") Integer matchId,@RequestBody Map<String,String> requestBody) throws DataAccessException{
        String idUser = requestBody.getOrDefault("user", "");
        User user = userService.findById(Integer.valueOf(idUser));
        String color = requestBody.getOrDefault("color", "");
        Match match = matchService.findById(matchId);
        Player p = new Player();
        p.setName(user.getName());
        p.setColor(Color.valueOf(color));
        p.setAlive(true);
        p.setEnergy(5);
        p.setUserPlayer(user);
        p.setMatch(match);
        p.setPoints(0);
        p.setPlayerOrder(match.getPlayersNumber());
        match.setPlayersNumber(match.getPlayersNumber() + 1);
        matchService.save(match);

        return new ResponseEntity<>(playerService.save(p), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/energy")
    public ResponseEntity<Player> updateEnergy(@PathVariable("id") Integer id, @RequestBody @Valid Integer energyUsed) throws ResourceNotFoundException, Exception {
        Player player = playerService.findById(id);
        if (player == null) {
            throw new ResourceNotFoundException("Jugador no encontrada", "id", id.toString());
        }
        if (player.getEnergy() - energyUsed < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        player.setEnergy(player.getEnergy() - energyUsed);
        playerService.save(player);
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @PatchMapping("/{id}/regenerateEnergy")
    public ResponseEntity<Player> regenerateEnergy(@PathVariable("id") Integer id) throws ResourceNotFoundException, Exception {
        Player player = playerService.findById(id);
        if (player == null) {
            throw new ResourceNotFoundException("Jugador no encontrada", "id", id.toString());
        }
        player.setEnergy(5);
        playerService.save(player);
        return new ResponseEntity<>(player, HttpStatus.OK);
    }
    
    
}