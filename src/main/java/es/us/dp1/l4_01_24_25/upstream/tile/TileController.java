package es.us.dp1.l4_01_24_25.upstream.tile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.general.BaseRestController;

@RestController
@RequestMapping("/api/v1/tiles")
public class TileController extends BaseRestController<Tile,Integer>{
 
    TileService tileService;

    @Autowired
    public TileController(TileService cs) {
        super(cs);
    }
}