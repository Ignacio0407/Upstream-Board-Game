package es.us.dp1.l4_01_24_25.upstream.chat;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        match.setState(State.EN_CURSO);
        match.setPhase(Phase.TILES);
        match.setPlayersNumber(2);
        match.setRound(1);
        match.setFinalScoreCalculated(false);
        entityManager.persist(match);

        User user = new User();
        user.setName("test");
        user.setName("test");
        user.setPassword("123");
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

        List<Message> messages = messageRepository.findAllMessagesByMatchId(match.getId());

        assertEquals(2, messages.size());
        assertEquals("Hola", messages.get(0).getContent());
        assertEquals("Mundo", messages.get(1).getContent());
    }

    @Test
    void testFindAllMessagesFromUser() {
        Message m1 = new Message(player, match, "Hola");
        m1.setCreatedAt(LocalDateTime.now());
        entityManager.persist(m1);

        List<Message> messages = messageRepository.findAllMessagesFromUser(player.getUserPlayer().getId());

        assertEquals(1, messages.size());
        assertEquals("Hola", messages.get(0).getContent());
    }

    @Test
    void testFindAllChatsFromUser() {
        Message m = new Message(player, match, "chat test");
        m.setCreatedAt(LocalDateTime.now());
        entityManager.persist(m);

        List<Match> result = messageRepository.findAllChatsFromUser(player.getUserPlayer().getId());

        assertEquals(1, result.size());
        assertEquals(match.getId(), result.get(0).getId());
    }

    @Test
    void testFindNewMessages() {
        LocalDateTime now = LocalDateTime.now();

        Message oldMsg = new Message(player, match, "Antiguo");
        oldMsg.setCreatedAt(now.minusMinutes(10));
        entityManager.persist(oldMsg);

        Message newMsg = new Message(player, match, "Nuevo");
        newMsg.setCreatedAt(now.minusSeconds(30));
        entityManager.persist(newMsg);

        List<Message> result = messageRepository.findNewMessages(match.getId(), now.minusMinutes(1));

        assertEquals(1, result.size());
        assertEquals("Nuevo", result.get(0).getContent());
    }
}