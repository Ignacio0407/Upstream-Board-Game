package upstream.tile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TileServiceTest {

    @Mock
    private TileRepository tileRepository;

    @InjectMocks
    private TileService tileService;

    @Test
    void testUpdateEntityFields() {
        Tile newTile = new Tile();
        newTile.setImage("new_img.png");
        newTile.setType(TileType.HERON);

        Tile tileToUpdate = new Tile();
        tileToUpdate.setImage("old_img.png");
        tileToUpdate.setType(TileType.WATER);

        Tile result = tileService.updateEntityFields(newTile, tileToUpdate);

        assertEquals("new_img.png", result.getImage());
        assertEquals(TileType.HERON, result.getType());
    }
}
