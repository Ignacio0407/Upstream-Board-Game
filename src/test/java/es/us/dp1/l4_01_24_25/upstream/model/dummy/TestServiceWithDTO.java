package es.us.dp1.l4_01_24_25.upstream.model.dummy;

import es.us.dp1.l4_01_24_25.upstream.model.BaseServiceWithDTO;
import es.us.dp1.l4_01_24_25.upstream.model.EntityMapper;

public class TestServiceWithDTO extends BaseServiceWithDTO<TestEntity, TestDTO, Integer> {
    public TestServiceWithDTO(TestRepository repository, EntityMapper<TestEntity, TestDTO> mapper) {
        super(repository, mapper);
    }

    @Override
    protected TestEntity updateEntityFields(TestEntity existing, TestEntity updated) {
        existing.setName(updated.getName());
        return existing;
    }
}
