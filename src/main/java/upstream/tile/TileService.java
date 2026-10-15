package upstream.tile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import upstream.model.BaseService;

@Service
public class TileService extends BaseService<Tile,Integer>{
    
    TileRepository tileRepository;

    public TileService(TileRepository tileRepository) {
        super(tileRepository);
    }

    @Override
    @Transactional
    protected Tile updateEntityFields (Tile newTile, Tile tileToUpdate) {
        tileToUpdate.setImage(newTile.getImage());
        tileToUpdate.setType(newTile.getType());
        return tileToUpdate;
    }
    
}