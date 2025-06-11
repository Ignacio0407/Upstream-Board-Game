package es.us.dp1.l4_01_24_25.upstream.tile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.us.dp1.l4_01_24_25.upstream.model.BaseService;

@Service
public class TileTypeService extends BaseService<TileType,Integer>{
    
    TileTypeRepository tileTypeRepository;

    @Autowired
    public TileTypeService(TileTypeRepository tileTypeRepository){
        super(tileTypeRepository);
    }
}