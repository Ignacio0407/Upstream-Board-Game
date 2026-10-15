package upstream.chat;

import upstream.match.Match;
import upstream.match.MatchService;
import upstream.player.Player;
import upstream.player.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageMapperTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private MatchService matchService;

    private MessageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MessageMapperImpl();
        mapper.playerService = playerService;
        mapper.matchService = matchService;
    }

    @Test
    void testToDTO_NullAndNonNull() {
        assertNull(mapper.toDTO((Message) null));

        Message msg = new Message();
        msg.setId(1);
        msg.setContent("Hello");
        Player p = new Player();
        p.setId(2);
        p.setName("Alice");
        Match m = new Match();
        m.setId(3);
        msg.setPlayer(p);
        msg.setMatch(m);

        MessageDTO dto = mapper.toDTO(msg);
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals("Hello", dto.getContent());
        assertEquals(2, dto.getPlayerId());
        assertEquals("Alice", dto.getPlayerName());
        assertEquals(3, dto.getMatchId());
    }

    @Test
    void testToEntity_NullAndNonNull() {
        assertNull(mapper.toEntity((MessageDTO) null));

        Player p = new Player();
        p.setId(2);
        Match m = new Match();
        m.setId(3);
        when(playerService.findById(2)).thenReturn(p);
        when(matchService.findById(3)).thenReturn(m);

        MessageDTO dto = new MessageDTO();
        dto.setId(1);
        dto.setContent("Hello");
        dto.setPlayerId(2);
        dto.setMatchId(3);

        Message entity = mapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(1, entity.getId());
        assertEquals("Hello", entity.getContent());
        assertEquals(p, entity.getPlayer());
        assertEquals(m, entity.getMatch());
    }

    @Test
    void testLists() {
        assertNull(mapper.toDTOList(null));
        assertTrue(mapper.toDTOList(Collections.emptyList()).isEmpty());

        Message msg = new Message();
        msg.setId(1);
        List<MessageDTO> dtoList = mapper.toDTOList(List.of(msg));
        assertEquals(1, dtoList.size());

        assertNull(mapper.toEntityList(null));
        assertTrue(mapper.toEntityList(Collections.emptyList()).isEmpty());

        MessageDTO dto = new MessageDTO();
        dto.setId(1);
        List<Message> entityList = mapper.toEntityList(List.of(dto));
        assertEquals(1, entityList.size());
    }

    @Test
    void testMapPlayerAndMatch() {
        assertNull(mapper.mapPlayer(null));
        Player p = new Player();
        when(playerService.findById(2)).thenReturn(p);
        assertEquals(p, mapper.mapPlayer(2));

        assertNull(mapper.mapMatch(null));
        Match m = new Match();
        when(matchService.findById(3)).thenReturn(m);
        assertEquals(m, mapper.mapMatch(3));
    }
}
