package es.us.dp1.l4_01_24_25.upstream.match;

import es.us.dp1.l4_01_24_25.upstream.match.DTO.MatchDTO;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
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
class MatchMapperTest {

    @Mock
    private UserService userService;

    @Mock
    private PlayerService playerService;

    private MatchMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MatchMapperImpl();
        mapper.userService = userService;
        mapper.playerService = playerService;
    }

    @Test
    void testToDTO_NullAndNonNull() {
        assertNull(mapper.toDTO((Match) null));

        Match match = new Match();
        match.setId(1);
        Player init = new Player();
        init.setId(10);
        Player act = new Player();
        act.setId(20);
        User creator = new User();
        creator.setId(30);

        match.setInitialPlayer(init);
        match.setActualPlayer(act);
        match.setMatchCreator(creator);

        MatchDTO dto = mapper.toDTO(match);
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals(10, dto.getInitialPlayerId());
        assertEquals(20, dto.getActualPlayerId());
        assertEquals(30, dto.getMatchCreatorId());
    }

    @Test
    void testToEntity_NullAndNonNull() {
        assertNull(mapper.toEntity((MatchDTO) null));

        Player init = new Player();
        init.setId(10);
        Player act = new Player();
        act.setId(20);
        User creator = new User();
        creator.setId(30);

        when(playerService.findById(10)).thenReturn(init);
        when(playerService.findById(20)).thenReturn(act);
        when(userService.findById(30)).thenReturn(creator);

        MatchDTO dto = new MatchDTO();
        dto.setId(1);
        dto.setInitialPlayerId(10);
        dto.setActualPlayerId(20);
        dto.setMatchCreatorId(30);

        Match entity = mapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(1, entity.getId());
        assertEquals(init, entity.getInitialPlayer());
        assertEquals(act, entity.getActualPlayer());
        assertEquals(creator, entity.getMatchCreator());
    }

    @Test
    void testLists() {
        assertNull(mapper.toDTOList(null));
        assertTrue(mapper.toDTOList(Collections.emptyList()).isEmpty());

        Match match = new Match();
        match.setId(1);
        List<MatchDTO> dtoList = mapper.toDTOList(List.of(match));
        assertEquals(1, dtoList.size());

        assertNull(mapper.toEntityList(null));
        assertTrue(mapper.toEntityList(Collections.emptyList()).isEmpty());

        MatchDTO dto = new MatchDTO();
        dto.setId(1);
        List<Match> entityList = mapper.toEntityList(List.of(dto));
        assertEquals(1, entityList.size());
    }

    @Test
    void testMapPlayerAndCreator() {
        assertNull(mapper.mapPlayer(null));
        Player p = new Player();
        when(playerService.findById(10)).thenReturn(p);
        assertEquals(p, mapper.mapPlayer(10));

        assertNull(mapper.mapMatchCreator(null));
        User u = new User();
        when(userService.findById(30)).thenReturn(u);
        assertEquals(u, mapper.mapMatchCreator(30));
    }
}
