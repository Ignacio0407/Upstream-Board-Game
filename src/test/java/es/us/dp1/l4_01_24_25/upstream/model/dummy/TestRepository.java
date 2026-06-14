package es.us.dp1.l4_01_24_25.upstream.model.dummy;

import es.us.dp1.l4_01_24_25.upstream.model.BaseRepository;
import es.us.dp1.l4_01_24_25.upstream.model.TestEntity;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TestRepository extends BaseRepository<TestEntity, TestDTO, Integer> {
}
