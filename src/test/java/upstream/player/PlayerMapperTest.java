package upstream.player;

import upstream.match.Match;
import upstream.match.MatchService;
import upstream.player.playerdto.LobbyPlayerDTO;
import upstream.player.playerdto.PlayerDTO;
import upstream.user.User;
import upstream.user.UserService;
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
class PlayerMapperTest {

    @Mock
    private UserService userService;

    @Mock
    private MatchService matchService;

    private PlayerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PlayerMapperImpl();
        mapper.userService = userService;
        mapper.matchService = matchService;
    }

    @Test
    void testToDTO_NullAndNonNull() {
        assertNull(mapper.toDTO((Player) null));

        Player player = new Player();
        player.setId(1);
        User u = new User();
        u.setId(10);
        Match m = new Match();
        m.setId(20);
        player.setUserPlayer(u);
        player.setMatch(m);

        PlayerDTO dto = mapper.toDTO(player);
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals(10, dto.getUserId());
        assertEquals(20, dto.getMatchId());
    }

    @Test
    void testToEntity_NullAndNonNull() {
        assertNull(mapper.toEntity((PlayerDTO) null));

        User u = new User();
        u.setId(10);
        Match m = new Match();
        m.setId(20);
        when(userService.findById(10)).thenReturn(u);
        when(matchService.findById(20)).thenReturn(m);

        PlayerDTO dto = new PlayerDTO();
        dto.setId(1);
        dto.setUserId(10);
        dto.setMatchId(20);

        Player entity = mapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(1, entity.getId());
        assertEquals(u, entity.getUserPlayer());
        assertEquals(m, entity.getMatch());
    }

    @Test
    void testLobbyMappings() {
        assertNull(mapper.toLobby(null));
        assertNull(mapper.toEntityFromLobby(null));

        Player player = new Player();
        player.setId(1);
        User u = new User();
        u.setId(10);
        Match m = new Match();
        m.setId(20);
        player.setUserPlayer(u);
        player.setMatch(m);

        LobbyPlayerDTO lobbyDTO = mapper.toLobby(player);
        assertNotNull(lobbyDTO);
        assertEquals(1, lobbyDTO.getId());
        assertEquals(10, lobbyDTO.getUserId());
        assertEquals(20, lobbyDTO.getMatchId());

        when(userService.findById(10)).thenReturn(u);
        when(matchService.findById(20)).thenReturn(m);

        Player playerFromLobby = mapper.toEntityFromLobby(lobbyDTO);
        assertNotNull(playerFromLobby);
        assertEquals(1, playerFromLobby.getId());
        assertEquals(u, playerFromLobby.getUserPlayer());
        assertEquals(m, playerFromLobby.getMatch());
    }

    @Test
    void testLists() {
        assertNull(mapper.toDTOList(null));
        assertTrue(mapper.toDTOList(Collections.emptyList()).isEmpty());

        Player player = new Player();
        player.setId(1);
        List<PlayerDTO> dtoList = mapper.toDTOList(List.of(player));
        assertEquals(1, dtoList.size());

        assertNull(mapper.toEntityList(null));
        assertTrue(mapper.toEntityList(Collections.emptyList()).isEmpty());

        PlayerDTO dto = new PlayerDTO();
        dto.setId(1);
        List<Player> entityList = mapper.toEntityList(List.of(dto));
        assertEquals(1, entityList.size());
    }

    @Test
    void testMapHelpers() {
        assertNull(mapper.mapUserPlayer(null));
        User u = new User();
        u.setId(10);
        when(userService.findById(10)).thenReturn(u);
        assertEquals(u, mapper.mapUserPlayer(10));

        assertNull(mapper.mapMatch(null));
        Match m = new Match();
        m.setId(20);
        when(matchService.findById(20)).thenReturn(m);
        assertEquals(m, mapper.mapMatch(20));

        assertNull(mapper.mapUserId(null));
        assertEquals(10, mapper.mapUserId(u));

        assertNull(mapper.mapMatchId(null));
        assertEquals(20, mapper.mapMatchId(m));
    }
}
