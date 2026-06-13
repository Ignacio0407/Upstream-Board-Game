package es.us.dp1.l4_01_24_25.upstream.chat;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private PlayerService playerService;

    @Mock
    private MatchService matchService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageService messageService;

    @Test
    @DisplayName("findUserMessages - Should return list of MessageDTO")
    void testFindUserMessages() {
        MessageDTO dto = new MessageDTO();

        when(messageRepository.findMessagesByUserIdDTO(1)).thenReturn(List.of(dto));

        List<MessageDTO> result = messageService.findUserMessages(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);
    }

    @Test
    @DisplayName("findUserChats - Should return list of Match")
    void testFindUserChats() {
        Match m1 = new Match();
        when(messageRepository.findAllChatsFromUser(1)).thenReturn(List.of(m1));

        List<Match> chats = messageService.findUserChats(1);

        assertThat(chats).hasSize(1);
        verify(messageRepository).findAllChatsFromUser(1);
    }

    @Test
    @DisplayName("sanitizeContent - Should remove HTML tags")
    void testSanitizeContent() {
        String dirty = "<script>alert('xss')</script><p>Hello</p>";
        String clean = MessageService.sanitizeContent(dirty);
        assertThat(clean).isEqualTo("Hello");
    }

    @Test
    @DisplayName("saveAndSend - Should sanitize, save and broadcast message")
    void testSaveAndSend() {
        MessageDTO dto = new MessageDTO();
        dto.setPlayerId(1);
        dto.setMatchId(1);
        dto.setContent("<p>Hello</p>");

        Player player = new Player();
        player.setId(1);
        Match match = new Match();
        match.setId(1);

        when(playerService.findById(1)).thenReturn(player);
        when(matchService.findById(1)).thenReturn(match);

        messageService.saveAndSend(dto);

        verify(messageRepository).save(any(Message.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/1"), any(Message.class));
    }
}
