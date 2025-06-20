package es.us.dp1.l4_01_24_25.upstream.tile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.model.BaseService;

@Service
public class TileService extends BaseService<Tile,Integer>{
    
    TileRepository tileRepository;

    public TileService(TileRepository tileRepository) {
        super(tileRepository);
    }

    @Override
    @Transactional
    protected void updateEntityFields (Tile newTile, Tile tileToUpdate) {
        tileToUpdate.setImage(newTile.getImage());
        tileToUpdate.setType(newTile.getType());
    }
    
}