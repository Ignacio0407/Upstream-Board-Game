package es.us.dp1.l4_01_24_25.upstream.matchtile;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.matchtile.dto.MatchTileDTO;
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
class MatchTileMapperTest {

    @Mock
    private MatchService matchService;

    private MatchTileMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MatchTileMapperImpl();
        mapper.matchService = matchService;
    }

    @Test
    void testToDTO_NullAndNonNull() {
        assertNull(mapper.toDTO((MatchTile) null));

        MatchTile matchTile = new MatchTile();
        matchTile.setId(1);
        Match match = new Match();
        match.setId(10);
        matchTile.setMatch(match);

        MatchTileDTO dto = mapper.toDTO(matchTile);
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals(10, dto.getMatchId());
    }

    @Test
    void testToEntity_NullAndNonNull() {
        assertNull(mapper.toEntity((MatchTileDTO) null));

        Match match = new Match();
        match.setId(10);
        when(matchService.findById(10)).thenReturn(match);

        MatchTileDTO dto = new MatchTileDTO();
        dto.setId(1);
        dto.setMatchId(10);

        MatchTile entity = mapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(1, entity.getId());
        assertEquals(match, entity.getMatch());
    }

    @Test
    void testLists() {
        assertNull(mapper.toDTOList(null));
        assertTrue(mapper.toDTOList(Collections.emptyList()).isEmpty());

        MatchTile tile = new MatchTile();
        tile.setId(1);
        List<MatchTileDTO> dtoList = mapper.toDTOList(List.of(tile));
        assertEquals(1, dtoList.size());

        assertNull(mapper.toEntityList(null));
        assertTrue(mapper.toEntityList(Collections.emptyList()).isEmpty());

        MatchTileDTO dto = new MatchTileDTO();
        dto.setId(1);
        List<MatchTile> entityList = mapper.toEntityList(List.of(dto));
        assertEquals(1, entityList.size());
    }

    @Test
    void testMapMatch() {
        assertNull(mapper.mapMatch(null));

        Match match = new Match();
        when(matchService.findById(5)).thenReturn(match);
        assertEquals(match, mapper.mapMatch(5));
    }

}