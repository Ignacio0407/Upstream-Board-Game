package es.us.dp1.l4_01_24_25.upstream.casilla;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface CasillaRepository extends CrudRepository<Casilla, Integer> {
    List<Casilla> findAll();
    Optional<Casilla> findById(int id);
}
