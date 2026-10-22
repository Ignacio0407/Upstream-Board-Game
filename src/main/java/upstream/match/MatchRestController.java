package upstream.match;

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

import jakarta.validation.Valid;
import upstream.exceptions.ResourceNotFoundException;
import upstream.match.dto.CreateMatchRequest;
import upstream.match.dto.DashboardMatchDTO;
import upstream.match.dto.MatchDTO;
import upstream.model.BaseRestControllerWithDTO;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchRestController extends BaseRestControllerWithDTO<Match, MatchDTO, Integer>{
    
    MatchService matchService;

    @Autowired
    public MatchRestController(MatchService matchService) {
        super(matchService);
        this.matchService = matchService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<DashboardMatchDTO>> findAllDashboard() {
        return ResponseEntity.ok(this.matchService.findAllAsDashboardDTO());
    }

    @PostMapping("/matchCreator/{userId}")
    public ResponseEntity<MatchDTO> createMatchWMatchCreator(
            @PathVariable("userId") Integer userId, 
            @Valid @RequestBody CreateMatchRequest request) {
        MatchDTO created = this.matchService.createMatchWMatchCreator(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).header("Content-Type", "application/json").body(created);
    }

    @PatchMapping("/{matchId}/startGame")
    public ResponseEntity<MatchDTO> startGame(@PathVariable("matchId") Integer matchId) throws ResourceNotFoundException {
        this.matchService.startGame(matchId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/{matchId}/changephase/{playerId}")
    public ResponseEntity<MatchDTO> changePhase(@PathVariable("matchId") Integer matchId, @PathVariable("playerId") Integer playerId) {
        this.matchService.changePhase(matchId, playerId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/finalscore/{id}")
    public ResponseEntity<MatchDTO> finalScore(@PathVariable Integer id) {
        return new ResponseEntity<>(this.matchService.finalScore(id), HttpStatus.OK);
    }
}