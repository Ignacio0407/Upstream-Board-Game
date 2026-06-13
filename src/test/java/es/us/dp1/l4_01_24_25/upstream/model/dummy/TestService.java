package es.us.dp1.l4_01_24_25.upstream.model.dummy;

import es.us.dp1.l4_01_24_25.upstream.model.BaseService;

public class TestService extends BaseService<TestEntity, Integer> {
    public TestService(TestRepository repository) {
        super(repository);
    }

    @Override
    protected TestEntity updateEntityFields(TestEntity existing, TestEntity updated) {
        existing.setName(updated.getName());
        return existing;
    }
}
