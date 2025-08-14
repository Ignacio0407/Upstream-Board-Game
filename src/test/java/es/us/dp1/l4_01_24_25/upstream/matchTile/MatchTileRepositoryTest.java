package es.us.dp1.l4_01_24_25.upstream.matchTile;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.match.Match;

import java.util.List;
import java.util.Optional;

@DataJpaTest
class MatchTileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MatchTileRepository matchTileRepository;

    @Test
    void findByMatchId_ShouldReturnTiles() {
        // Setup
        Match match = new Match();
        entityManager.persist(match);
        
        MatchTile tile1 = new MatchTile();
        tile1.setMatch(match);
        entityManager.persist(tile1);
        
        MatchTile tile2 = new MatchTile();
        tile2.setMatch(match);
        entityManager.persist(tile2);

        // Test
        List<MatchTile> result = matchTileRepository.findByMatchId(match.getId());

        // Verify
        assertEquals(2, result.size());
    }

    @Test
    void findByMatchIdNoCoord_ShouldReturnOnlyTilesWithoutCoordinates() {
        // Setup
        Match match = new Match();
        entityManager.persist(match);
        
        MatchTile tile1 = new MatchTile();
        tile1.setMatch(match);
        tile1.setCoordinate(new Coordinate(1, 1));
        entityManager.persist(tile1);
        
        MatchTile tile2 = new MatchTile();
        tile2.setMatch(match);
        entityManager.persist(tile2);

        // Test
        List<MatchTile> result = matchTileRepository.findByMatchIdAndCoordinateIsNull(match.getId());

        // Verify
        assertEquals(1, result.size());
        assertNull(result.get(0).getCoordinate());
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, true",
        "2, 2, false"
    })
    void findByCoordinate_ShouldReturnCorrectTile(int x, int y, boolean shouldExist) {
        // Setup
        MatchTile tile = new MatchTile();
        tile.setCoordinate(new Coordinate(1, 1));
        entityManager.persist(tile);

        // Test
        Optional<MatchTile> result = matchTileRepository.findByCoordinate(x, y);

        // Verify
        assertEquals(shouldExist, result.isPresent());
    }
}