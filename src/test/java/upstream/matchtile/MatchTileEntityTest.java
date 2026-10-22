package upstream.matchtile;

import upstream.coordinate.Coordinate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchTileEntityTest {

    @Test
    void testIsFull_WhenSalmonsReachOrExceedCapacity() {
        MatchTile matchTile = new MatchTile();
        matchTile.setCapacity(2);

        matchTile.setSalmonsNumber(1);
        assertFalse(matchTile.isFull());

        matchTile.setSalmonsNumber(2);
        assertTrue(matchTile.isFull());

        matchTile.setSalmonsNumber(3);
        assertTrue(matchTile.isFull());
    }

    @Test
    void testGettersAndSetters() {
        MatchTile matchTile = new MatchTile();
        Coordinate c = new Coordinate(1, 2);
        matchTile.setCapacity(3);
        matchTile.setOrientation(0);
        matchTile.setSalmonsNumber(1);
        matchTile.setCoordinate(c);

        assertEquals(3, matchTile.getCapacity());
        assertEquals(0, matchTile.getOrientation());
        assertEquals(1, matchTile.getSalmonsNumber());
        assertEquals(c, matchTile.getCoordinate());
    }
}
