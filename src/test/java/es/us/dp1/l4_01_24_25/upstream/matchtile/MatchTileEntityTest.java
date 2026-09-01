package es.us.dp1.l4_01_24_25.upstream.matchtile;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchTileEntityTest {

    @Test
    void testIsFull_PlayersGreaterThanTwo_And_RockTile() {
        Match match = new Match();
        match.setPlayersNumber(3);

        Tile rockTile = new Tile();
        rockTile.setType(TileType.ROCK);

        MatchTile matchTile = new MatchTile();
        matchTile.setMatch(match);
        matchTile.setTile(rockTile);
        matchTile.setCapacity(2);

        // Capacity 2 equals salmonsNumber (3) - 1 => True
        matchTile.setSalmonsNumber(3);
        assertTrue(matchTile.isFull());

        // Capacity 2 != salmonsNumber (2) - 1 => False
        matchTile.setSalmonsNumber(2);
        assertFalse(matchTile.isFull());
    }

    @Test
    void testIsFull_PlayersTwoOrLess_Or_NonRockTile() {
        Match match = new Match();
        match.setPlayersNumber(2);

        Tile rockTile = new Tile();
        rockTile.setType(TileType.ROCK);

        MatchTile matchTile = new MatchTile();
        matchTile.setMatch(match);
        matchTile.setTile(rockTile);
        matchTile.setCapacity(2);

        // match <= 2: capacity 2 equals salmonsNumber 2 => True
        matchTile.setSalmonsNumber(2);
        assertTrue(matchTile.isFull());

        matchTile.setSalmonsNumber(1);
        assertFalse(matchTile.isFull());

        // match > 2 but non-rock tile (WATER)
        match.setPlayersNumber(4);
        Tile waterTile = new Tile();
        waterTile.setType(TileType.WATER);
        matchTile.setTile(waterTile);

        matchTile.setSalmonsNumber(2);
        assertTrue(matchTile.isFull());

        matchTile.setSalmonsNumber(3);
        assertFalse(matchTile.isFull());
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
