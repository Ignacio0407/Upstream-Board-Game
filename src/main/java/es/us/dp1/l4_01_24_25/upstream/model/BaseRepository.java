package es.us.dp1.l4_01_24_25.upstream.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, D, ID> extends JpaRepository<T, ID>{

    List<D> findAllAsDTO();

    Optional<D> findByIdAsDTO(ID id);
}