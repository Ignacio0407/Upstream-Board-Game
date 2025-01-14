package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MatchTileRepositoryTest {

    @Mock
    private MatchTileRepository matchTileRepository;

    @Test
    void testFindByMatchId() {
        Integer validMatchId = 1;
        Integer invalidMatchId = 2;

        MatchTile matchTile = new MatchTile();
        matchTile.setId(1);

        when(matchTileRepository.findByMatchId(validMatchId)).thenReturn(List.of(matchTile));
        when(matchTileRepository.findByMatchId(invalidMatchId)).thenReturn(Collections.emptyList());

        List<MatchTile> validResult = matchTileRepository.findByMatchId(validMatchId);
        List<MatchTile> invalidResult = matchTileRepository.findByMatchId(invalidMatchId);

        assertFalse(validResult.isEmpty());
        assertSame(matchTile, validResult.get(0));

        assertTrue(invalidResult.isEmpty());
    }


    @Test
    void testFindWithNoCoord() {
        Integer validMatchId = 1;
        Integer invalidMatchId = 2;

        MatchTile matchTile = new MatchTile();
        matchTile.setId(1);
        matchTile.setCoordinate(null);

        when(matchTileRepository.findWithNoCoord(validMatchId)).thenReturn(List.of(matchTile));
        when(matchTileRepository.findWithNoCoord(invalidMatchId)).thenReturn(Collections.emptyList());

        List<MatchTile> validResult = matchTileRepository.findWithNoCoord(validMatchId);
        List<MatchTile> invalidResult = matchTileRepository.findWithNoCoord(invalidMatchId);

        assertFalse(validResult.isEmpty());
        assertSame(matchTile, validResult.get(0));

        assertTrue(invalidResult.isEmpty());
    }


    @Test
    void testFindByCoordinate() {
        Integer x = 5;
        Integer y = 10;
        Integer invalidX = 1;
        Integer invalidY = 2;

        MatchTile matchTile = new MatchTile();
        matchTile.setId(1);

        when(matchTileRepository.findByCoordinate(x, y)).thenReturn(matchTile);
        when(matchTileRepository.findByCoordinate(invalidX, invalidY)).thenReturn(null);

        MatchTile validResult = matchTileRepository.findByCoordinate(x, y);
        MatchTile invalidResult = matchTileRepository.findByCoordinate(invalidX, invalidY);

        assertSame(matchTile, validResult);
        assertNull(invalidResult);
    }
}