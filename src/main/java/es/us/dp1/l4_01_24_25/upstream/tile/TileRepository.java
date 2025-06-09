package es.us.dp1.l4_01_24_25.upstream.tile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("override")
public interface TileRepository extends JpaRepository<Tile, Integer> {
}