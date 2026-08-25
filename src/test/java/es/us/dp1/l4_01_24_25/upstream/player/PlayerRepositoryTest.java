package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.Phase;
import es.us.dp1.l4_01_24_25.upstream.match.State;
import es.us.dp1.l4_01_24_25.upstream.player.playerDTO.PlayerDTO;
import es.us.dp1.l4_01_24_25.upstream.user.Authority;
import es.us.dp1.l4_01_24_25.upstream.user.User;

@DataJpaTest
class PlayerRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PlayerRepository playerRepository;

    private Match match;
    private Match otherMatch;
    private Player player1;
    private Player player2;
    private Player deadPlayer;
    private User user1;
    private User user2;

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

        user1 = new User();
        user1.setName("userOne");
        user1.setPassword("pass");
        user1.setAuthority(Authority.PLAYER);
        em.persist(user1);

        user2 = new User();
        user2.setName("userTwo");
        user2.setPassword("pass");
        user2.setAuthority(Authority.PLAYER);
        em.persist(user2);

        player1 = new Player();
        player1.setName("Alpha");
        player1.setColor(Color.RED);
        player1.setPlayerOrder(1);
        player1.setAlive(true);
        player1.setPoints(5);
        player1.setEnergy(3);
        player1.setMatch(match);
        player1.setUserPlayer(user1);
        em.persist(player1);

        player2 = new Player();
        player2.setName("Beta");
        player2.setColor(Color.YELLOW);
        player2.setPlayerOrder(2);
        player2.setAlive(true);
        player2.setPoints(10);
        player2.setEnergy(2);
        player2.setMatch(match);
        player2.setUserPlayer(user2);
        em.persist(player2);

        deadPlayer = new Player();
        deadPlayer.setName("Dead");
        deadPlayer.setColor(Color.GREEN);
        deadPlayer.setPlayerOrder(3);
        deadPlayer.setAlive(false);
        deadPlayer.setPoints(0);
        deadPlayer.setEnergy(0);
        deadPlayer.setMatch(match);
        deadPlayer.setUserPlayer(user2);
        em.persist(deadPlayer);

        em.flush();
    }

    // ── findPlayersByMatchAsDTO ───────────────────────────────────────────────

    @Test
    void shouldReturnPlayersDTOForMatch() {
        List<PlayerDTO> result = playerRepository.findPlayersByMatchAsDTO(match.getId());
        assertThat(result).hasSize(3);
        assertThat(result).anyMatch(p -> p.getName().equals("Alpha"));
        assertThat(result).anyMatch(p -> p.getName().equals("Beta"));
    }

    @Test
    void shouldReturnEmptyDTOWhenMatchHasNoPlayers() {
        List<PlayerDTO> result = playerRepository.findPlayersByMatchAsDTO(otherMatch.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnPlayersFromOtherMatchAsDTO() {
        List<PlayerDTO> result = playerRepository.findPlayersByMatchAsDTO(otherMatch.getId());
        assertThat(result).noneMatch(p -> p.getName().equals("Alpha"));
    }

    // ── findByMatchIdOrderByPlayerOrderAscAsDTO ───────────────────────────────

    @Test
    void shouldReturnPlayersDTOOrderedByPlayerOrder() {
        List<PlayerDTO> result = playerRepository.findByMatchIdOrderByPlayerOrderAscAsDTO(match.getId());
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result.get(0).getPlayerOrder()).isLessThanOrEqualTo(result.get(1).getPlayerOrder());
    }

    @Test
    void shouldReturnEmptyOrderedDTOWhenMatchHasNoPlayers() {
        List<PlayerDTO> result = playerRepository.findByMatchIdOrderByPlayerOrderAscAsDTO(otherMatch.getId());
        assertThat(result).isEmpty();
    }

    // ── findByMatchIdAndAliveTrue ─────────────────────────────────────────────

    @Test
    void shouldReturnOnlyAlivePlayersForMatch() {
        List<Player> result = playerRepository.findByMatchIdAndAliveTrue(match.getId());
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Player::getAlive);
        assertThat(result).noneMatch(p -> p.getName().equals("Dead"));
    }

    @Test
    void shouldReturnEmptyWhenNoAlivePlayers() {
        List<Player> result = playerRepository.findByMatchIdAndAliveTrue(otherMatch.getId());
        assertThat(result).isEmpty();
    }

    // ── findByMatchIdAndAliveTrueOrderByPlayerOrderAsc ────────────────────────

    @Test
    void shouldReturnAlivePlayersOrderedByPlayerOrder() {
        List<Player> result = playerRepository.findByMatchIdAndAliveTrueOrderByPlayerOrderAsc(match.getId());
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPlayerOrder()).isLessThan(result.get(1).getPlayerOrder());
    }

    // ── existsByUsernameAndMatchId ────────────────────────────────────────────

    @Test
    void shouldReturnTrueWhenUserIsInMatch() {
        Boolean result = playerRepository.existsByUsernameAndMatchId("userOne", match.getId());
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserIsNotInMatch() {
        Boolean result = playerRepository.existsByUsernameAndMatchId("userOne", otherMatch.getId());
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        Boolean result = playerRepository.existsByUsernameAndMatchId("noexiste", match.getId());
        assertThat(result).isFalse();
    }
}