package es.us.dp1.l4_01_24_25.upstream.match;

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

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.model.BaseRestController;
import es.us.dp1.l4_01_24_25.upstream.player.Player;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchRestController extends BaseRestController<Match, Integer>{
    
    MatchService matchService;

    @Autowired
    public MatchRestController(MatchService matchService) {
        super(matchService);
    }

    // TODO
    @GetMapping("/user/{id}")
    public ResponseEntity<List<Match>> findMatchesFromUser() {
        return null;
    }

    @GetMapping("/{id}/players")
    public ResponseEntity<List<Player>> findPlayersFromGame(@PathVariable("id") Integer id) throws ResourceNotFoundException {
        return ResponseEntity.ok(matchService.findList(matchService.findPlayersFromGame(id)));
    }   

    @PatchMapping("/{matchId}/ronda")
    public ResponseEntity<Match> updateRound(@PathVariable("matchId") Integer matchId) throws ResourceNotFoundException {
        return ResponseEntity.ok(matchService.findById(matchId));
    }

    @PostMapping("/matchCreator/{userId}")
    public ResponseEntity<Match> createMatchWMatchCreator(@PathVariable("userId") Integer userId, @RequestBody Map<String, String> requestBody) { 
        return new ResponseEntity<>(matchService.createMatchWMatchCreator(userId, requestBody), HttpStatus.CREATED);
    }

    @PatchMapping("/{matchId}/startGame")
    public ResponseEntity<Match> startGame(@PathVariable("matchId") Integer matchId) throws ResourceNotFoundException {
        return new ResponseEntity<>(matchService.startGame(matchId), HttpStatus.OK);
    }

    public ResponseEntity<Match> changePhase(@PathVariable("matchId") Integer matchId, @PathVariable("playerId") Integer playerId) {
        Match updatedMatch = matchService.changePhase(matchId, playerId);
        return new ResponseEntity<>(updatedMatch, HttpStatus.OK);
    }

    @PatchMapping("/finalscore/{id}")
    public ResponseEntity<Match> finalScore(@PathVariable Integer id) {
        return new ResponseEntity<>(matchService.finalScore(id), HttpStatus.OK);
    }
}