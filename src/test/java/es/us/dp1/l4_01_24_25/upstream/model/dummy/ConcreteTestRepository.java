package es.us.dp1.l4_01_24_25.upstream.model.dummy;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.us.dp1.l4_01_24_25.upstream.model.BaseRepository;
import es.us.dp1.l4_01_24_25.upstream.model.TestEntity;

@Repository
public interface ConcreteTestRepository extends BaseRepository<TestEntity, TestDTO, Integer> {

    @Override
    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.model.dummy.TestDTO(e.id, e.name) FROM TestEntity e WHERE e.id = :id")
    Optional<TestDTO> findByIdAsDTO(@Param("id") Integer id);

    @Override
    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.model.dummy.TestDTO(e.id, e.name) FROM TestEntity e")
    List<TestDTO> findAllAsDTO();
}
