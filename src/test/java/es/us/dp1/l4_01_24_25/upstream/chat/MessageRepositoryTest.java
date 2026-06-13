package es.us.dp1.l4_01_24_25.upstream.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessageRepository messageRepository;

    private Player player;
    private Match match;

    @BeforeEach
    void setup() {
        match = new Match();
        match.setState(State.ON_GOING);
        match.setPhase(Phase.TILES);
        match.setPlayersNumber(2);
        match.setRound(1);
        match.setFinalScoreCalculated(false);
        entityManager.persist(match);

        User user = new User();
        user.setName("test");
        user.setPassword("123");
        user.setAuthority(Authority.PLAYER);
        entityManager.persist(user);

        player = new Player();
        player.setAlive(true);
        player.setColor(Color.RED);
        player.setEnergy(5);
        player.setName("Jugador 1");
        player.setPoints(0);
        player.setPlayerOrder(1);
        player.setMatch(match);
        player.setUserPlayer(user);
        entityManager.persist(player);
    }

    @Test
    void testFindAllMessagesByMatchId() {
        Message m1 = new Message(player, match, "Hola");
        m1.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        entityManager.persist(m1);

        Message m2 = new Message(player, match, "Mundo");
        m2.setCreatedAt(LocalDateTime.now());
        entityManager.persist(m2);

        List<MessageDTO> messages = messageRepository.findMessagesByMatchIdDTO(match.getId());

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getContent()).isEqualTo("Hola");
        assertThat(messages.get(1).getContent()).isEqualTo("Mundo");
    }

    @Test
    void testFindAllMessagesFromUser() {
        Message m1 = new Message(player, match, "Hola");
        m1.setCreatedAt(LocalDateTime.now());
        entityManager.persist(m1);

        List<MessageDTO> messages = messageRepository.findMessagesByUserIdDTO(player.getUserPlayer().getId());

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).isEqualTo("Hola");
    }

    @Test
    void testFindAllChatsFromUser() {
        Message m = new Message(player, match, "chat test");
        m.setCreatedAt(LocalDateTime.now());
        entityManager.persist(m);

        List<Match> result = messageRepository.findAllChatsFromUser(player.getUserPlayer().getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(match.getId());
    }

    @Test
    void testFindByIdAsDTO() {
        Message m = new Message(player, match, "dto test");
        entityManager.persist(m);

        Optional<MessageDTO> result = messageRepository.findByIdAsDTO(m.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("dto test");
        assertThat(result.get().getPlayerName()).isEqualTo(player.getName());
    }

    @Test
    void testFindAllAsDTO() {
        Message m1 = new Message(player, match, "dto test 1");
        entityManager.persist(m1);
        Message m2 = new Message(player, match, "dto test 2");
        entityManager.persist(m2);

        List<MessageDTO> result = messageRepository.findAllAsDTO();

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).extracting(MessageDTO::getContent)
                .contains("dto test 1", "dto test 2");
    }
}

