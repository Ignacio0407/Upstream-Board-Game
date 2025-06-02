package es.us.dp1.l4_01_24_25.upstream.tile;

import org.springframework.stereotype.Service;
import es.us.dp1.l4_01_24_25.upstream.general.BaseService;


@Service
public class TileService extends BaseService<Tile,Integer>{
    
    TileRepository tileRepository;

    public TileService(TileRepository tileRepository) {
        super(tileRepository);
    }
}