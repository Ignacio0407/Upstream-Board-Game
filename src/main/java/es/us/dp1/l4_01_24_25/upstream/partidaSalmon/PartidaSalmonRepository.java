package es.us.dp1.l4_01_24_25.upstream.partidaSalmon;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface PartidaSalmonRepository extends CrudRepository<PartidaSalmon, Integer>{
    @SuppressWarnings("null")
    @Override
    Optional<PartidaSalmon> findById(Integer Id);


}
