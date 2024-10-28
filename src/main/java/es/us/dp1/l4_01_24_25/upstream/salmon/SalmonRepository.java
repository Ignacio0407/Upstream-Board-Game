package es.us.dp1.l4_01_24_25.upstream.salmon;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface  SalmonRepository extends  CrudRepository<Salmon, Integer>{
    List<Salmon> findAll();
    Optional<Salmon> findById(int id);
    
}
