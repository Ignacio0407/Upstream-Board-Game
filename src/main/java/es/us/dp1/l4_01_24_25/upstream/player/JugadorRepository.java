package es.us.dp1.l4_01_24_25.upstream.player;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  JugadorRepository extends CrudRepository<Jugador, Integer>{
    
    List<Jugador> findAll();

    public Jugador findByName(String name);
}
