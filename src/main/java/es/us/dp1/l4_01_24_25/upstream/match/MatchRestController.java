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
import es.us.dp1.l4_01_24_25.upstream.model.BaseRestControllerWithDTO;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchRestController extends BaseRestControllerWithDTO<Match, MatchDTO, Integer>{
    
    MatchService matchService;

    @Autowired
    public MatchRestController(MatchService matchService) {
        super(matchService);
        this.matchService = matchService;
    }

    // TODO
    @GetMapping("/user/{id}")
    public ResponseEntity<List<Match>> findMatchesFromUser() {
        return null;
    } 

    @PatchMapping("/{matchId}/ronda")
    public ResponseEntity<MatchDTO> updateRound(@PathVariable("matchId") Integer matchId) throws ResourceNotFoundException {
        return ResponseEntity.ok(matchService.updateRound(matchId));
    }

    @PostMapping("/matchCreator/{userId}")
    public ResponseEntity<MatchDTO> createMatchWMatchCreator(@PathVariable("userId") Integer userId, @RequestBody Map<String, String> requestBody) { 
        return new ResponseEntity<>(matchService.createMatchWMatchCreator(userId, requestBody), HttpStatus.CREATED);
    }

    @PatchMapping("/{matchId}/startGame")
    public ResponseEntity<MatchDTO> startGame(@PathVariable("matchId") Integer matchId) throws ResourceNotFoundException {
        return new ResponseEntity<>(matchService.startGame(matchId), HttpStatus.OK);
    }

    @PatchMapping("/{matchId}/changephase/{playerId}")
    public ResponseEntity<MatchDTO> changePhase(@PathVariable("matchId") Integer matchId, @PathVariable("playerId") Integer playerId) {
        return new ResponseEntity<>(matchService.changePhase(matchId, playerId), HttpStatus.OK);
    }

    @PatchMapping("/finalscore/{id}")
    public ResponseEntity<MatchDTO> finalScore(@PathVariable Integer id) {
        return new ResponseEntity<>(matchService.finalScore(id), HttpStatus.OK);
    }
}