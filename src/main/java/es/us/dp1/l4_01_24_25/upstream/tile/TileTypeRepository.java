package es.us.dp1.l4_01_24_25.upstream.tile;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface TileTypeRepository extends CrudRepository<TileType, Integer> {
    Optional<TileType> findById(Integer id);
}
