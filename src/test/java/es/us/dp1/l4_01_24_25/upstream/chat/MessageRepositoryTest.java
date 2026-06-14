package es.us.dp1.l4_01_24_25.upstream.chat;

import java.time.LocalDateTime;
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
import es.us.dp1.l4_01_24_25.upstream.player.Color;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.user.Authority;
import es.us.dp1.l4_01_24_25.upstream.user.User;

@DataJpaTest
public class MessageRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private MessageRepository messageRepository;

    private Player player;
    private Player player2;
    private Match match;
    private Match match2;

    @BeforeEach
    void setUp() {
        match = new Match();
        match.setState(State.ON_GOING);
        match.setPhase(Phase.TILES);
        match.setPlayersNumber(2);
        match.setRound(1);
        match.setFinalScoreCalculated(false);
        em.persist(match);

        match2 = new Match();
        match2.setState(State.WAITING);
        match2.setPhase(Phase.TILES);
        match2.setPlayersNumber(2);
        match2.setRound(0);
        match2.setFinalScoreCalculated(false);
        em.persist(match2);

        User user = new User();
        user.setName("testuser");
        user.setPassword("pass");
        user.setAuthority(Authority.PLAYER);
        em.persist(user);

        User user2 = new User();
        user2.setName("testuser2");
        user2.setPassword("pass");
        user2.setAuthority(Authority.PLAYER);
        em.persist(user2);

        player = new Player();
        player.setAlive(true);
        player.setColor(Color.RED);
        player.setEnergy(5);
        player.setName("Jugador 1");
        player.setPoints(0);
        player.setPlayerOrder(1);
        player.setMatch(match);
        player.setUserPlayer(user);
        em.persist(player);

        player2 = new Player();
        player2.setAlive(true);
        player2.setColor(Color.WHITE);
        player2.setEnergy(3);
        player2.setName("Jugador 2");
        player2.setPoints(0);
        player2.setPlayerOrder(2);
        player2.setMatch(match2);
        player2.setUserPlayer(user2);
        em.persist(player2);

        em.flush();
    }

    // ── findMessagesByMatchIdDTO ──────────────────────────────────────────────

    @Test
    void shouldReturnMessagesOrderedByDateForMatch() {
        Message m1 = new Message(player, match, "Primero");
        m1.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        em.persist(m1);

        Message m2 = new Message(player, match, "Segundo");
        m2.setCreatedAt(LocalDateTime.now());
        em.persist(m2);
        em.flush();

        List<MessageDTO> result = messageRepository.findMessagesByMatchIdDTO(match.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("Primero");
        assertThat(result.get(1).getContent()).isEqualTo("Segundo");
    }

    @Test
    void shouldReturnEmptyWhenMatchHasNoMessages() {
        List<MessageDTO> result = messageRepository.findMessagesByMatchIdDTO(match.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnMessagesFromOtherMatch() {
        em.persist(new Message(player2, match2, "Otro match"));
        em.flush();

        List<MessageDTO> result = messageRepository.findMessagesByMatchIdDTO(match.getId());

        assertThat(result).isEmpty();
    }

    // ── findMessagesByUserIdDTO ───────────────────────────────────────────────

    @Test
    void shouldReturnMessagesForUser() {
        Message m1 = new Message(player, match, "Msg A");
        m1.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        em.persist(m1);

        Message m2 = new Message(player, match, "Msg B");
        m2.setCreatedAt(LocalDateTime.now());
        em.persist(m2);
        em.flush();

        List<MessageDTO> result = messageRepository.findMessagesByUserIdDTO(player.getUserPlayer().getId());

        assertThat(result).hasSize(2);
        // ORDER BY createdAt DESC — más reciente primero
        assertThat(result.get(0).getContent()).isEqualTo("Msg B");
        assertThat(result.get(1).getContent()).isEqualTo("Msg A");
    }

    @Test
    void shouldReturnEmptyWhenUserHasNoMessages() {
        List<MessageDTO> result = messageRepository.findMessagesByUserIdDTO(player.getUserPlayer().getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnMessagesFromOtherUser() {
        em.persist(new Message(player2, match2, "No mío"));
        em.flush();

        List<MessageDTO> result = messageRepository.findMessagesByUserIdDTO(player.getUserPlayer().getId());

        assertThat(result).isEmpty();
    }

    // ── findAllChatsFromUser ──────────────────────────────────────────────────

    @Test
    void shouldReturnDistinctMatchesWhereUserHasMessages() {
        em.persist(new Message(player, match, "Uno"));
        em.persist(new Message(player, match, "Dos"));
        em.flush();

        List<Match> result = messageRepository.findAllChatsFromUser(player.getUserPlayer().getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(match.getId());
    }

    @Test
    void shouldReturnEmptyChatsWhenUserHasNoMessages() {
        List<Match> result = messageRepository.findAllChatsFromUser(player.getUserPlayer().getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnChatsFromOtherUser() {
        em.persist(new Message(player2, match2, "Otro"));
        em.flush();

        List<Match> result = messageRepository.findAllChatsFromUser(player.getUserPlayer().getId());

        assertThat(result).isEmpty();
    }
}