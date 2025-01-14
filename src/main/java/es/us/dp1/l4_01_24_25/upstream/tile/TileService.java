package es.us.dp1.l4_01_24_25.upstream.tile;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TileService {
    
    TileRepository tileRepository;

    public TileService(TileRepository cr) {
        this.tileRepository = cr;
    }

    @Transactional(readOnly = true)
    public List<Tile> findAll() {
        return tileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Tile> findById(int id) {
        return tileRepository.findById(id);
    }

}
