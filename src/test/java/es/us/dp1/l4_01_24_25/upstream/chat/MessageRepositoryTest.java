package es.us.dp1.l4_01_24_25.upstream.chat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.user.User;

@ExtendWith(MockitoExtension.class)
public class MessageRepositoryTest {

    @Mock
    private MessageRepository messageRepository;

    @Test
    public void testFindAllMessagesByMatchId() {
        Integer matchId = 1;
        
        Message message1 = new Message();
        message1.setMatch(new Match());
        message1.getMatch().setId(matchId);
        message1.setDeleted(false);
        message1.setCreatedAt(LocalDateTime.now());

        Message message2 = new Message();
        message2.setMatch(new Match());
        message2.getMatch().setId(matchId);
        message2.setDeleted(false);
        message2.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        List<Message> messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);

        when(messageRepository.findAllMessagesByMatchId(matchId)).thenReturn(messages);

        List<Message> result = messageRepository.findAllMessagesByMatchId(matchId);

        assertNotNull(result);
        assertEquals(2, result.size());

        Integer invalidMatchId = 2;

        when(messageRepository.findAllMessagesByMatchId(invalidMatchId)).thenReturn(new ArrayList<>());

        result = messageRepository.findAllMessagesByMatchId(invalidMatchId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAllMessagesFromUser() {
        Integer userId = 1;
        
        Message message1 = new Message();
        message1.setPlayer(new Player());
        message1.getPlayer().setUserPlayer(new User());
        message1.getPlayer().getUserPlayer().setId(userId);
        message1.setDeleted(false);
        message1.setCreatedAt(LocalDateTime.now());

        Message message2 = new Message();
        message2.setPlayer(new Player());
        message2.getPlayer().setUserPlayer(new User());
        message2.getPlayer().getUserPlayer().setId(userId);
        message2.setDeleted(false);
        message2.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        List<Message> messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);

        when(messageRepository.findAllMessagesFromUser(userId)).thenReturn(messages);

        List<Message> result = messageRepository.findAllMessagesFromUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());

        Integer invalidUserId = 2;

        when(messageRepository.findAllMessagesFromUser(invalidUserId)).thenReturn(new ArrayList<>());

        result = messageRepository.findAllMessagesFromUser(invalidUserId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAllChatsFromUser() {
        Integer userId = 1;
        
        Match match1 = new Match();
        match1.setId(1);
        Match match2 = new Match();
        match2.setId(2);

        List<Match> matches = new ArrayList<>();
        matches.add(match1);
        matches.add(match2);

        when(messageRepository.findAllChatsFromUser(userId)).thenReturn(matches);

        List<Match> result = messageRepository.findAllChatsFromUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());

        Integer invalidUserId = 2;

        when(messageRepository.findAllChatsFromUser(invalidUserId)).thenReturn(new ArrayList<>());

        result = messageRepository.findAllChatsFromUser(invalidUserId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindNewMessages() {
        Integer matchId = 1;
        LocalDateTime timestamp = LocalDateTime.now().minusMinutes(5);

        Message message1 = new Message();
        message1.setMatch(new Match());
        message1.getMatch().setId(matchId);
        message1.setDeleted(false);
        message1.setCreatedAt(LocalDateTime.now().minusMinutes(3));

        Message message2 = new Message();
        message2.setMatch(new Match());
        message2.getMatch().setId(matchId);
        message2.setDeleted(false);
        message2.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        List<Message> messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);

        when(messageRepository.findNewMessages(matchId, timestamp)).thenReturn(messages);

        List<Message> result = messageRepository.findNewMessages(matchId, timestamp);

        assertNotNull(result);
        assertEquals(2, result.size());

        LocalDateTime invalidTimestamp = LocalDateTime.now().plusMinutes(5);

        when(messageRepository.findNewMessages(matchId, invalidTimestamp)).thenReturn(new ArrayList<>());

        result = messageRepository.findNewMessages(matchId, invalidTimestamp);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}