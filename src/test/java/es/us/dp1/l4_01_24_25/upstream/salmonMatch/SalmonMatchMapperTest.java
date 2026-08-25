package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SalmonMatchDTO;
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
class SalmonMatchMapperTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private MatchService matchService;

    private SalmonMatchMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SalmonMatchMapperImpl();
        mapper.playerService = playerService;
        mapper.matchService = matchService;
    }

    @Test
    void testToDTO_NullAndNonNull() {
        assertNull(mapper.toDTO((SalmonMatch) null));

        SalmonMatch sm = new SalmonMatch();
        sm.setId(1);
        Player p = new Player();
        p.setId(2);
        Match m = new Match();
        m.setId(3);
        sm.setPlayer(p);
        sm.setMatch(m);

        SalmonMatchDTO dto = mapper.toDTO(sm);
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals(2, dto.getPlayerId());
        assertEquals(3, dto.getMatchId());
    }

    @Test
    void testToEntity_NullAndNonNull() {
        assertNull(mapper.toEntity((SalmonMatchDTO) null));

        Player p = new Player();
        p.setId(2);
        Match m = new Match();
        m.setId(3);
        when(playerService.findById(2)).thenReturn(p);
        when(matchService.findById(3)).thenReturn(m);

        SalmonMatchDTO dto = new SalmonMatchDTO();
        dto.setId(1);
        dto.setPlayerId(2);
        dto.setMatchId(3);

        SalmonMatch sm = mapper.toEntity(dto);
        assertNotNull(sm);
        assertEquals(1, sm.getId());
        assertEquals(p, sm.getPlayer());
        assertEquals(m, sm.getMatch());
    }

    @Test
    void testLists() {
        assertNull(mapper.toDTOList(null));
        assertTrue(mapper.toDTOList(Collections.emptyList()).isEmpty());

        SalmonMatch sm = new SalmonMatch();
        sm.setId(1);
        List<SalmonMatchDTO> dtoList = mapper.toDTOList(List.of(sm));
        assertEquals(1, dtoList.size());

        assertNull(mapper.toEntityList(null));
        assertTrue(mapper.toEntityList(Collections.emptyList()).isEmpty());

        SalmonMatchDTO dto = new SalmonMatchDTO();
        dto.setId(1);
        List<SalmonMatch> entityList = mapper.toEntityList(List.of(dto));
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