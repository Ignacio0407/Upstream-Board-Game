package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MatchRepositoryTest {

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void findByName_returnsCorrectMatch() {
        Match match = new Match();
        match.setName("Prueba");
        match.setPhase(Phase.TILES);
        match.setState(State.ESPERANDO);
        match.setRound(0);
        match.setPlayersNumber(0);
        match.setFinalScoreCalculated(false);
        entityManager.persist(match);

        Optional<Match> result = matchRepository.findByName("Prueba");

        assertTrue(result.isPresent());
        assertEquals("Prueba", result.get().getName());
    }

    @Test
    void findHeronWithCoordFromGame_returnsEmptyListIfNoMatchTile() {
        Match match = new Match();
        match.setName("Heron");
        match.setPhase(Phase.TILES);
        match.setState(State.ESPERANDO);
        match.setRound(0);
        match.setPlayersNumber(0);
        match.setFinalScoreCalculated(false);
        entityManager.persist(match);

        List<MatchTile> tiles = matchRepository.findHeronWithCoordFromGame(match.getId());

        assertTrue(tiles.isEmpty());
    }
}