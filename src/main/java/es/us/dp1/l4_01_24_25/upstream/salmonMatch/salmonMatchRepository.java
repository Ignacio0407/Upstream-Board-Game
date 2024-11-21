package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface salmonMatchRepository extends CrudRepository<salmonMatch, Integer>{
    @SuppressWarnings("null")
    @Override
    Optional<salmonMatch> findById(Integer Id);


}
