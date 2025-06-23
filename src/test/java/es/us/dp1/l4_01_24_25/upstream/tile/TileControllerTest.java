package es.us.dp1.l4_01_24_25.upstream.tile;

import java.util.List;

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
        tile1.setType(TileType.WATER);

        Tile tile2 = new Tile();
        tile2.setId(2);
        tile2.setImage("rojo2");
        tile2.setType(TileType.EAGLE);

        List<Tile> tiles = List.of(tile1, tile2);

        when(tileService.findAll()).thenReturn(tiles);

        mockMvc.perform(get("/api/v1/tiles"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                            {"id": 1, "image": "rojo1", "type": "WATER"},
                            {"id": 2, "image": "rojo2", "type": "EAGLE"}
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
        tile.setId(1); tile.setImage("rojo1");;
        tile.setType(TileType.WATER);

        //when(tileService.findById(1)).thenReturn(Optional.of(tile));

        mockMvc.perform(get("/api/v1/tiles/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"id": 1, "image": "rojo1", "type": "WATER"}
                        """));
    }

    @Test
    public void testFindTileById_NotFound() throws Exception {
        //when(tileService.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/tiles/1"))
                .andExpect(status().isNotFound());
    }
}

