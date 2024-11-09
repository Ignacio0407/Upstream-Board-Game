package es.us.dp1.l4_01_24_25.upstream.casilla;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface TipoCasillaRepository extends CrudRepository<TipoCasilla, Integer> {
    Optional<TipoCasilla> findById(Integer id);
}
