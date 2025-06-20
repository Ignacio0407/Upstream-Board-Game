package es.us.dp1.l4_01_24_25.upstream.chat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchRepository;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;
    
    @Mock
    private PlayerRepository playerRepository;
    
    @Mock
    private MatchRepository matchRepository;
    
    @InjectMocks
    private MessageService messageService;

    private Message message1;
    private Message message2;
    private Player testPlayer;
    private Match testMatch;

    @BeforeEach
    void setup() {
        testPlayer = new Player();
        testPlayer.setId(1);
        
        testMatch = new Match();
        testMatch.setId(1);

        message1 = new Message(testPlayer, testMatch, "Test message 1");
        message1.setId(1);
        message1.setCreatedAt(LocalDateTime.now());
        
        message2 = new Message(testPlayer, testMatch, "Test message 2");
        message2.setId(2);
        message2.setCreatedAt(LocalDateTime.now().minusMinutes(5));
    }

    @Nested
    @DisplayName("GET Operations Tests")
    class GetOperationsTests {
        
        @Test
        void testGetMatchMessages_Success() {
            List<Message> expectedMessages = Arrays.asList(message2, message1);
            when(messageRepository.findAllMessagesByMatchId(1)).thenReturn(expectedMessages);

            List<Message> result = messageService.findMatchMessages(1);

            assertNotNull(result);
            assertEquals(expectedMessages, result);
            verify(messageRepository).findAllMessagesByMatchId(1);
        }

        @Test
        void testGetMatchMessages_NotFound() {
            when(messageRepository.findAllMessagesByMatchId(99)).thenReturn(null);

            List<Message> result = messageService.findMatchMessages(99);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(messageRepository).findAllMessagesByMatchId(99);
        }

        @Test
        void testGetUserMessages_Success() {
            List<Message> expectedMessages = Arrays.asList(message2, message1);
            when(messageRepository.findAllMessagesFromUser(1)).thenReturn(expectedMessages);

            List<Message> result = messageService.findUserMessages(1);

            assertNotNull(result);
            assertEquals(expectedMessages, result);
            verify(messageRepository).findAllMessagesFromUser(1);
        }

        @Test
        void testGetUserChats_Success() {
            List<Match> expectedChats = Arrays.asList(testMatch);
            when(messageRepository.findAllChatsFromUser(1)).thenReturn(expectedChats);

            List<Match> result = messageService.findUserChats(1);

            assertNotNull(result);
            assertEquals(expectedChats, result);
            verify(messageRepository).findAllChatsFromUser(1);
        }

        @Test
        void testGetNewMessages_Success() {
            List<Message> expectedMessages = Arrays.asList(message1);
            // Usamos eq(1) para el primer argumento y any() para el segundo
            when(messageRepository.findNewMessages(eq(1), any(LocalDateTime.class))).thenReturn(expectedMessages);

            // Llamada al método con matchers para ambos argumentos
            List<Message> result = messageService.findNewMessages(1, LocalDateTime.now().minusMinutes(10));

            assertNotNull(result);
            assertEquals(expectedMessages, result);
            // Verificación también con matchers
            verify(messageRepository).findNewMessages(eq(1), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("POST Operations Tests")
    class PostOperationsTests {

        @Test
        void testCreateMessage_Success() {
            when(playerRepository.findById(1)).thenReturn(Optional.of(testPlayer));
            when(matchRepository.findById(1)).thenReturn(Optional.of(testMatch));
            when(messageRepository.save(any(Message.class))).thenReturn(message1);

            Message result = messageService.create(1, 1, "Test message 1");

            assertNotNull(result);
            assertEquals("Test message 1", result.getContent());
            assertEquals(testPlayer, result.getPlayer());
            assertEquals(testMatch, result.getMatch());
            verify(messageRepository).save(any(Message.class));
        }

        @Test
        void testCreateMessage_PlayerNotFound() {
            when(playerRepository.findById(1)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                messageService.create(1, 1, "Test message");
            });

            assertEquals("Player not found", exception.getMessage());
        }

        @Test
        void testCreateMessage_MatchNotFound() {
            when(playerRepository.findById(1)).thenReturn(Optional.of(testPlayer));
            when(matchRepository.findById(1)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                messageService.create(1, 1, "Test message");
            });

            assertEquals("Match not found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("DELETE Operations Tests")
    class DeleteOperationsTests {

        @Test
        void testDeleteMessage_Success() {
            when(messageRepository.findById(1)).thenReturn(Optional.of(message1));

            messageService.delete(1);

            verify(messageRepository).delete(message1);
        }

        @Test
        void testDeleteMessage_NotFound() {
            when(messageRepository.findById(99)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                messageService.delete(99);
            });

            assertEquals("Message not found", exception.getMessage());
        }
    }
}