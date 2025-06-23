package es.us.dp1.l4_01_24_25.upstream.chat;

import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @InjectMocks
    private MessageService messageService;

    @Test
    void testFindMatchMessagesAsDTO() {
        Message m1 = new Message();
        Message m2 = new Message();
        MessageDTO dto1 = new MessageDTO();
        MessageDTO dto2 = new MessageDTO();

        when(messageRepository.findAllMessagesByMatchId(1)).thenReturn(List.of(m1, m2));
        when(messageMapper.toDTO(m1)).thenReturn(dto1);
        when(messageMapper.toDTO(m2)).thenReturn(dto2);

        List<MessageDTO> result = messageService.findMatchMessagesAsDTO(1);

        assertEquals(2, result.size());
        verify(messageRepository).findAllMessagesByMatchId(1);
        verify(messageMapper).toDTO(m1);
        verify(messageMapper).toDTO(m2);
    }

    @Test
    void testFindUserMessages() {
        Message m = new Message();
        MessageDTO dto = new MessageDTO();

        when(messageRepository.findAllMessagesFromUser(1)).thenReturn(List.of(m));
        when(messageMapper.toDTO(m)).thenReturn(dto);

        List<MessageDTO> result = messageService.findUserMessages(1);

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }

    @Test
    void testFindUserChats() {
        Match m1 = new Match();
        when(messageRepository.findAllChatsFromUser(1)).thenReturn(List.of(m1));

        List<Match> chats = messageService.findUserChats(1);

        assertEquals(1, chats.size());
        verify(messageRepository).findAllChatsFromUser(1);
    }

    @Test
    void testFindNewMessages() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        Message m = new Message();
        when(messageRepository.findNewMessages(1, since)).thenReturn(List.of(m));

        List<Message> result = messageService.findNewMessages(1, since);

        assertEquals(1, result.size());
        verify(messageRepository).findNewMessages(1, since);
    }

    @Test
    void testSaveAsDTO() {
        MessageDTO dto = new MessageDTO();
        dto.setPlayerId(5);
        dto.setMatchId(10);
        dto.setContent("Hello");

        Player p = new Player();
        Match m = new Match();
        //Message msg = new Message(p, m, "Hello");
        MessageDTO dtoResult = new MessageDTO();

        when(playerService.findById(5)).thenReturn(p);
        when(matchService.findById(10)).thenReturn(m);
        when(messageMapper.toDTO(any(Message.class))).thenReturn(dtoResult);

        MessageDTO result = messageService.saveAsDTO(dto);

        assertEquals(dtoResult, result);
        verify(messageRepository).save(any(Message.class));
    }
}