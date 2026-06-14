package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.Phase;
import es.us.dp1.l4_01_24_25.upstream.match.State;
import es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MTCoordinateDTO;
import es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MatchTileDTO;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileType;

@DataJpaTest
public class MatchTileRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private MatchTileRepository matchTileRepository;

    private Match match;
    private Match otherMatch;
    private Tile regularTile;
    private Tile heronTile;

    // tileWithCoord → coordinate (1,10), regularTile
    // tileNoCoord   → coordinate null,   regularTile
    // heronWithCoord→ coordinate (2,15), heronTile
    private MatchTile tileWithCoord;
    private MatchTile tileNoCoord;
    private MatchTile heronWithCoord;

    @BeforeEach
    void setUp() {
        match = new Match();
        match.setState(State.ON_GOING);
        match.setPhase(Phase.TILES);
        match.setPlayersNumber(2);
        match.setRound(1);
        match.setFinalScoreCalculated(false);
        em.persist(match);

        otherMatch = new Match();
        otherMatch.setState(State.WAITING);
        otherMatch.setPhase(Phase.TILES);
        otherMatch.setPlayersNumber(2);
        otherMatch.setRound(0);
        otherMatch.setFinalScoreCalculated(false);
        em.persist(otherMatch);

        regularTile = new Tile();
        regularTile.setType(TileType.WATER);
        regularTile.setImage("/images/tiles/water.webp");
        em.persist(regularTile);

        heronTile = new Tile();
        heronTile.setType(TileType.HERON);
        heronTile.setImage("/images/tiles/heron.webp");
        em.persist(heronTile);

        tileWithCoord = new MatchTile();
        tileWithCoord.setMatch(match);
        tileWithCoord.setTile(regularTile);
        tileWithCoord.setCoordinate(new Coordinate(1, 10));
        tileWithCoord.setCapacity(2);
        tileWithCoord.setOrientation(0);
        tileWithCoord.setSalmonsNumber(0);
        em.persist(tileWithCoord);

        tileNoCoord = new MatchTile();
        tileNoCoord.setMatch(match);
        tileNoCoord.setTile(regularTile);
        tileNoCoord.setCoordinate(null);
        tileNoCoord.setCapacity(2);
        tileNoCoord.setOrientation(0);
        tileNoCoord.setSalmonsNumber(0);
        em.persist(tileNoCoord);

        heronWithCoord = new MatchTile();
        heronWithCoord.setMatch(match);
        heronWithCoord.setTile(heronTile);
        heronWithCoord.setCoordinate(new Coordinate(2, 15));
        heronWithCoord.setCapacity(1);
        heronWithCoord.setOrientation(1);
        heronWithCoord.setSalmonsNumber(0);
        em.persist(heronWithCoord);

        em.flush();
    }

    // ── findByMatchIdAsDTO ────────────────────────────────────────────────────

    @Test
    void shouldReturnAllMatchTilesForMatchAsDTO() {
        List<MatchTileDTO> result = matchTileRepository.findByMatchIdAsDTO(match.getId());
        assertThat(result).hasSize(3);
    }

    @Test
    void shouldReturnEmptyDTOWhenMatchHasNoTiles() {
        List<MatchTileDTO> result = matchTileRepository.findByMatchIdAsDTO(otherMatch.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnTilesFromOtherMatchAsDTO() {
        MatchTile foreign = new MatchTile();
        foreign.setMatch(otherMatch);
        foreign.setTile(regularTile);
        foreign.setCoordinate(new Coordinate(0, 5));
        foreign.setCapacity(2);
        foreign.setOrientation(0);
        foreign.setSalmonsNumber(0);
        em.persistAndFlush(foreign);

        List<MatchTileDTO> result = matchTileRepository.findByMatchIdAsDTO(match.getId());
        assertThat(result).hasSize(3);
        assertThat(result).noneMatch(dto -> dto.getMatchId().equals(otherMatch.getId()));
    }

    // ── findByMatchIdAndCoordinateIsNullAsDTO ─────────────────────────────────

    @Test
    void shouldReturnOnlyNullCoordinateTilesAsDTO() {
        List<MatchTileDTO> result = matchTileRepository.findByMatchIdAndCoordinateIsNullAsDTO(match.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoordinate()).isNull();
    }

    @Test
    void shouldReturnEmptyNullCoordDTOWhenMatchHasNoTiles() {
        List<MatchTileDTO> result = matchTileRepository.findByMatchIdAndCoordinateIsNullAsDTO(otherMatch.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnTilesWithCoordinateInNullCoordQuery() {
        List<MatchTileDTO> result = matchTileRepository.findByMatchIdAndCoordinateIsNullAsDTO(match.getId());
        assertThat(result).noneMatch(dto -> dto.getCoordinate() != null);
    }

    // ── findHeronWithCoordFromGame ────────────────────────────────────────────

    @Test
    void shouldReturnHeronTilesWithCoordinateForMatch() {
        List<MatchTile> result = matchTileRepository.findHeronWithCoordFromGame(match.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTile().getType()).isEqualTo(TileType.HERON);
        assertThat(result.get(0).getCoordinate()).isNotNull();
    }

    @Test
    void shouldReturnEmptyWhenNoHeronTilesWithCoord() {
        List<MatchTile> result = matchTileRepository.findHeronWithCoordFromGame(otherMatch.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnNonHeronTilesFromHeronQuery() {
        List<MatchTile> result = matchTileRepository.findHeronWithCoordFromGame(match.getId());
        assertThat(result).noneMatch(mt -> mt.getTile().getType() != TileType.HERON);
    }

    // ── findByCoordinate ──────────────────────────────────────────────────────

    @Test
    void shouldReturnTileWhenCoordinateMatches() {
        Optional<MatchTile> result = matchTileRepository.findByCoordinate(1, 10);
        assertThat(result).isPresent();
        assertThat(result.get().getCoordinate().x()).isEqualTo(1);
        assertThat(result.get().getCoordinate().y()).isEqualTo(10);
    }

    @Test
    void shouldReturnEmptyWhenCoordinateDoesNotMatch() {
        Optional<MatchTile> result = matchTileRepository.findByCoordinate(0, 0);
        assertThat(result).isEmpty();
    }

    // ── findByMatchIdNoCoordAsMTCoordinateDTO ─────────────────────────────────

    @Test
    void shouldReturnNullCoordTilesAsMTCoordinateDTO() {
        List<MTCoordinateDTO> result = matchTileRepository.findByMatchIdNoCoordAsMTCoordinateDTO(match.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoordinate()).isNull();
    }

    @Test
    void shouldReturnEmptyMTCoordinateDTOWhenMatchHasNoNullCoordTiles() {
        List<MTCoordinateDTO> result = matchTileRepository.findByMatchIdNoCoordAsMTCoordinateDTO(otherMatch.getId());
        assertThat(result).isEmpty();
    }

    // ── findByMatchIdAsMTCoordinateDTO ────────────────────────────────────────

    @Test
    void shouldReturnAllTilesAsMTCoordinateDTO() {
        List<MTCoordinateDTO> result = matchTileRepository.findByMatchIdAsMTCoordinateDTO(match.getId());
        assertThat(result).hasSize(3);
    }

    @Test
    void shouldReturnEmptyMTCoordinateDTOWhenMatchHasNoTiles() {
        List<MTCoordinateDTO> result = matchTileRepository.findByMatchIdAsMTCoordinateDTO(otherMatch.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnMixOfNullAndNonNullCoordinatesAsMTCoordinateDTO() {
        List<MTCoordinateDTO> result = matchTileRepository.findByMatchIdAsMTCoordinateDTO(match.getId());
        assertThat(result).anyMatch(dto -> dto.getCoordinate() == null);
        assertThat(result).anyMatch(dto -> dto.getCoordinate() != null);
    }
}