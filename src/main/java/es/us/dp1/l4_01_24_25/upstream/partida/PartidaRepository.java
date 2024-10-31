package es.us.dp1.l4_01_24_25.upstream.partida;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartidaRepository extends CrudRepository<Partida, Integer>{
    
    @SuppressWarnings("null")
    @Override
    List<Partida> findAll();
    
    public Partida findByName(String name);

}