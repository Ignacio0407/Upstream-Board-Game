package es.us.dp1.l4_01_24_25.upstream.model.dummy;

import es.us.dp1.l4_01_24_25.upstream.model.BaseService;
import es.us.dp1.l4_01_24_25.upstream.model.TestEntity;

public class NoOverrideTestService extends BaseService<TestEntity, Integer> {
    public NoOverrideTestService(TestRepository repository) {
        super(repository);
    }
}
