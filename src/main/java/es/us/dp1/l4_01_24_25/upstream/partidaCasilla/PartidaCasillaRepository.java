package es.us.dp1.l4_01_24_25.upstream.partidaCasilla;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface PartidaCasillaRepository extends CrudRepository<PartidaCasilla,Integer> {
    @SuppressWarnings("null")
    @Override
	Optional<PartidaCasilla> findById(Integer id);
    

    
}
