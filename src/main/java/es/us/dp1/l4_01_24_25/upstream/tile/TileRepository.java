package es.us.dp1.l4_01_24_25.upstream.tile;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface TileRepository extends CrudRepository<Tile, Integer> {
    List<Tile> findAll();
    Optional<Tile> findById(int id);
}
