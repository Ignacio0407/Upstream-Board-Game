package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;

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
import es.us.dp1.l4_01_24_25.upstream.player.Color;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SMCoordinateDTO;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SalmonMatchDTO;
import es.us.dp1.l4_01_24_25.upstream.user.Authority;
import es.us.dp1.l4_01_24_25.upstream.user.User;

@DataJpaTest
class SalmonMatchRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private SalmonMatchRepository salmonMatchRepository;

    private Match match;
    private Match otherMatch;
    private Player player;
    private Player otherPlayer;

    private SalmonMatch smRiver;  // coordinate.y < 20
    private SalmonMatch smSpawn;  // coordinate.y > 20
    private Salmon salmon;

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

        User user = new User();
        user.setName("userOne");
        user.setPassword("pass");
        user.setAuthority(Authority.PLAYER);
        em.persist(user);

        User otherUser = new User();
        otherUser.setName("userTwo");
        otherUser.setPassword("pass");
        otherUser.setAuthority(Authority.PLAYER);
        em.persist(otherUser);

        player = new Player();
        player.setName("Alpha");
        player.setColor(Color.RED);
        player.setPlayerOrder(1);
        player.setAlive(true);
        player.setPoints(0);
        player.setEnergy(3);
        player.setMatch(match);
        player.setUserPlayer(user);
        em.persist(player);

        otherPlayer = new Player();
        otherPlayer.setName("Beta");
        otherPlayer.setColor(Color.YELLOW);
        otherPlayer.setPlayerOrder(2);
        otherPlayer.setAlive(true);
        otherPlayer.setPoints(0);
        otherPlayer.setEnergy(3);
        otherPlayer.setMatch(otherMatch);
        otherPlayer.setUserPlayer(otherUser);
        em.persist(otherPlayer);

        salmon = em.find(Salmon.class, 1);
        
        smRiver = new SalmonMatch();
        smRiver.setMatch(match);
        smRiver.setPlayer(player);
        smRiver.setCoordinate(new Coordinate(1, 10)); // y < 20 → river
        smRiver.setSalmonsNumber(2);
        smRiver.setSpawningNumber(0);
        smRiver.setSalmon(salmon);
        em.persist(smRiver);

        smSpawn = new SalmonMatch();
        smSpawn.setMatch(match);
        smSpawn.setPlayer(player);
        smSpawn.setCoordinate(new Coordinate(1, 22)); // y > 20 → spawn
        smSpawn.setSalmonsNumber(1);
        smSpawn.setSpawningNumber(1);
        smSpawn.setSalmon(salmon);
        em.persist(smSpawn);

        em.flush();
    }

    // ── findByMatchIdAsDTO ────────────────────────────────────────────────────

    @Test
    void shouldReturnAllSalmonMatchesForMatchAsDTO() {
        List<SalmonMatchDTO> result = salmonMatchRepository.findByMatchIdAsDTO(match.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyDTOWhenMatchHasNoSalmonMatches() {
        List<SalmonMatchDTO> result = salmonMatchRepository.findByMatchIdAsDTO(otherMatch.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnSalmonMatchesFromOtherMatchAsDTO() {
        SalmonMatch foreign = new SalmonMatch();
        foreign.setMatch(otherMatch);
        foreign.setPlayer(otherPlayer);
        foreign.setCoordinate(new Coordinate(0, 5));
        foreign.setSalmonsNumber(1);
        foreign.setSpawningNumber(0);
        foreign.setSalmon(salmon);
        em.persistAndFlush(foreign);

        List<SalmonMatchDTO> result = salmonMatchRepository.findByMatchIdAsDTO(match.getId());
        assertThat(result).hasSize(2);
        assertThat(result).noneMatch(dto -> dto.getMatchId().equals(otherMatch.getId()));
    }

    // ── findByPlayerIdAsDTO ───────────────────────────────────────────────────

    @Test
    void shouldReturnAllSalmonMatchesForPlayerAsDTO() {
        List<SalmonMatchDTO> result = salmonMatchRepository.findByPlayerIdAsDTO(player.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyDTOWhenPlayerHasNoSalmonMatches() {
        List<SalmonMatchDTO> result = salmonMatchRepository.findByPlayerIdAsDTO(otherPlayer.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnSalmonMatchesFromOtherPlayerAsDTO() {
        List<SalmonMatchDTO> result = salmonMatchRepository.findByPlayerIdAsDTO(otherPlayer.getId());
        assertThat(result).noneMatch(dto -> dto.getPlayerId().equals(player.getId()));
    }

    // ── findFromGameInSpawn / findFromGameInSpawnAsDTO ────────────────────────

    @Test
    void shouldReturnOnlySalmonMatchesInSpawnForMatch() {
        List<SalmonMatch> result = salmonMatchRepository.findFromGameInSpawn(match.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoordinate().y()).isGreaterThan(20);
    }

    @Test
    void shouldReturnEmptyWhenNoSalmonMatchesInSpawnForMatch() {
        List<SalmonMatch> result = salmonMatchRepository.findFromGameInSpawn(otherMatch.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnSpawnSalmonMatchesAsDTO() {
        List<SalmonMatchDTO> result = salmonMatchRepository.findFromGameInSpawnAsDTO(match.getId());
        assertThat(result).hasSize(1);
    }

    // ── findAllFromMatchInRiver ───────────────────────────────────────────────

    @Test
    void shouldReturnOnlySalmonMatchesInRiverForMatch() {
        List<SalmonMatch> result = salmonMatchRepository.findAllFromMatchInRiver(match.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoordinate().y()).isLessThan(20);
    }

    @Test
    void shouldReturnEmptyWhenNoSalmonMatchesInRiverForMatch() {
        List<SalmonMatch> result = salmonMatchRepository.findAllFromMatchInRiver(otherMatch.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnSpawnSalmonMatchesFromRiverQuery() {
        List<SalmonMatch> result = salmonMatchRepository.findAllFromMatchInRiver(match.getId());
        assertThat(result).noneMatch(sm -> sm.getCoordinate().y() > 20);
    }

    // ── findAllFromPlayerInRiver ──────────────────────────────────────────────

    @Test
    void shouldReturnSalmonMatchesInRiverForPlayer() {
        List<SalmonMatch> result = salmonMatchRepository.findAllFromPlayerInRiver(player.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoordinate().y()).isLessThan(20);
    }

    @Test
    void shouldReturnEmptyRiverResultForPlayerWithNoRiverSalmons() {
        List<SalmonMatch> result = salmonMatchRepository.findAllFromPlayerInRiver(otherPlayer.getId());
        assertThat(result).isEmpty();
    }

    // ── findAllFromPlayerInSpawn ──────────────────────────────────────────────

    @Test
    void shouldReturnSalmonMatchesInSpawnForPlayer() {
        List<SalmonMatch> result = salmonMatchRepository.findAllFromPlayerInSpawn(player.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoordinate().y()).isGreaterThan(20);
    }

    @Test
    void shouldReturnEmptySpawnResultForPlayerWithNoSpawnSalmons() {
        List<SalmonMatch> result = salmonMatchRepository.findAllFromPlayerInSpawn(otherPlayer.getId());
        assertThat(result).isEmpty();
    }

    // ── findByPlayerIdAsCoordinateDTO ─────────────────────────────────────────

    @Test
    void shouldReturnAllCoordinatesForPlayer() {
        List<SMCoordinateDTO> result = salmonMatchRepository.findByPlayerIdAsCoordinateDTO(player.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyCoordinatesForPlayerWithNoSalmons() {
        List<SMCoordinateDTO> result = salmonMatchRepository.findByPlayerIdAsCoordinateDTO(otherPlayer.getId());
        assertThat(result).isEmpty();
    }

    // ── findByPlayerIdInRiverAsCoordinateDTO ──────────────────────────────────

    @Test
    void shouldReturnOnlyRiverCoordinatesForPlayer() {
        List<SMCoordinateDTO> result = salmonMatchRepository.findByPlayerIdInRiverAsCoordinateDTO(player.getId());
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnEmptyRiverCoordinatesForPlayerWithNoRiverSalmons() {
        List<SMCoordinateDTO> result = salmonMatchRepository.findByPlayerIdInRiverAsCoordinateDTO(otherPlayer.getId());
        assertThat(result).isEmpty();
    }
}