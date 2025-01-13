package es.us.dp1.l4_01_24_25.upstream.tile;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = TileController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class))
@AutoConfigureMockMvc(addFilters = false)
public class TileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TileService tileService;

    @Test
    public void testFindAllTiles_Success() throws Exception {
        Tile tile1 = new Tile();
        tile1.setId(1);
        tile1.setImage("rojo1");
        TileType tiletype1 = new TileType();
        tiletype1.setId(1);
        tiletype1.setType("AGUA");
        tile1.setType(tiletype1);

        Tile tile2 = new Tile();
        tile2.setId(2);
        tile2.setImage("rojo2");
        TileType tiletype2 = new TileType();
        tiletype2.setId(2);
        tiletype2.setType("AGUILA");
        tile2.setType(tiletype2);

        List<Tile> tiles = List.of(tile1, tile2);

        when(tileService.findAll()).thenReturn(tiles);

        mockMvc.perform(get("/api/v1/tiles"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                            {"id": 1, "image": "rojo1", "type": "AGUA"},
                            {"id": 2, "image": "rojo2", "type": "AGUILA"}
                        ]
                        """));
    }


    @Test
    public void testFindAllTiles_Empty() throws Exception {
        when(tileService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/tiles"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void testFindTileById_Success() throws Exception {
        Tile tile = new Tile();
        tile.setId(1); tile.setImage("rojo1");
        TileType tiletype = new TileType();
        tiletype.setType("AGUA");
        tile.setType(tiletype);

        when(tileService.findById(1)).thenReturn(Optional.of(tile));

        mockMvc.perform(get("/api/v1/tiles/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"id": 1, "image": "rojo1", "type": "AGUA"}
                        """));
    }

    @Test
    public void testFindTileById_NotFound() throws Exception {
        when(tileService.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/tiles/1"))
                .andExpect(status().isNotFound());
    }
}

