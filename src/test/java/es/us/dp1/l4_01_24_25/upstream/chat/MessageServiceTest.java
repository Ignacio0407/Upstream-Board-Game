package es.us.dp1.l4_01_24_25.upstream.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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

    // ── sanitizeContent ───────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
        "<script>alert('xss')</script>Hello,          Hello",
        "<p>Normal text</p>,                          Normal text",
        "<img src=x onerror=alert(1)>clean,           clean",
        "<b><i>nested</i></b>,                        nested",
        "no tags at all,                              no tags at all"
    })
    void shouldSanitizeContent(String input, String expected) {
        assertThat(MessageService.sanitizeContent(input.strip())).isEqualTo(expected.strip());
    }

    @Test
    void shouldReturnEmptyStringWhenContentIsOnlyTags() {
        assertThat(MessageService.sanitizeContent("<script>alert(1)</script>")).isEmpty();
    }

    // ── saveAndSend ───────────────────────────────────────────────────────────

    @Test
    void shouldSanitizeContentBeforeSaving() {
        MessageDTO dto = new MessageDTO();
        dto.setPlayerId(1);
        dto.setMatchId(1);
        dto.setContent("<script>alert('xss')</script>Hello");

        when(playerService.findById(1)).thenReturn(new Player());
        Match match = new Match();
        match.setId(1);
        when(matchService.findById(1)).thenReturn(match);
        when(messageMapper.toDTO(any(Message.class))).thenReturn(dto);

        messageService.saveAndSend(dto);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("Hello");
    }

    @Test
    void shouldBroadcastToCorrectTopic() {
        MessageDTO dto = new MessageDTO();
        dto.setPlayerId(1);
        dto.setMatchId(42);
        dto.setContent("Hola");

        when(playerService.findById(1)).thenReturn(new Player());
        Match match = new Match();
        match.setId(42);
        when(matchService.findById(42)).thenReturn(match);
        when(messageMapper.toDTO(any(Message.class))).thenReturn(dto);

        messageService.saveAndSend(dto);

        verify(messagingTemplate).convertAndSend(eq("/topic/chat/42"), eq(dto));
    }

    @Test
    void shouldSaveAndBroadcastCleanMessage() {
        MessageDTO dto = new MessageDTO();
        dto.setPlayerId(1);
        dto.setMatchId(1);
        dto.setContent("Mensaje limpio");

        when(playerService.findById(1)).thenReturn(new Player());
        Match match = new Match();
        match.setId(1);
        when(matchService.findById(1)).thenReturn(match);
        when(messageMapper.toDTO(any(Message.class))).thenReturn(dto);

        messageService.saveAndSend(dto);

        verify(messageRepository).save(any(Message.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/1"), eq(dto));
    }

    @Test
    void testUpdateEntityFields() {
        Message newMessage = new Message();
        newMessage.setContent("Updated content");

        Message messageToUpdate = new Message();
        messageToUpdate.setContent("Old content");

        Message result = messageService.updateEntityFields(newMessage, messageToUpdate);

        assertThat(result.getContent()).isEqualTo("Updated content");
    }

    @Test
    void testFindQueries() {
        MessageDTO dto = new MessageDTO();
        when(messageRepository.findMessagesByMatchIdDTO(1)).thenReturn(List.of(dto));
        assertThat(messageService.findMatchMessages(1)).containsExactly(dto);

        when(messageRepository.findMessagesByUserIdDTO(1)).thenReturn(List.of(dto));
        assertThat(messageService.findUserMessages(1)).containsExactly(dto);

        Match match = new Match();
        when(messageRepository.findAllChatsFromUser(1)).thenReturn(List.of(match));
        assertThat(messageService.findUserChats(1)).containsExactly(match);
    }
}
