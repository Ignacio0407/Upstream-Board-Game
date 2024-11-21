package es.us.dp1.l4_01_24_25.upstream.tile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;

public class TileTypeService {
    
    private final TileTypeRepository tileTypeRepository;

    @Autowired
    public TileTypeService(TileTypeRepository tipoCasillaRepository){
        
        this.tileTypeRepository = tipoCasillaRepository;
    }

    @Transactional
    public TileType findById(Integer id) throws ResourceNotFoundException {
        return tileTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("TipoCasilla", "id", id));
    }

}
