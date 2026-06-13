package es.us.dp1.l4_01_24_25.upstream.model.dummy;

import es.us.dp1.l4_01_24_25.upstream.model.BaseRestController;
import es.us.dp1.l4_01_24_25.upstream.model.BaseService;

public class TestController extends BaseRestController<TestEntity, Integer> {
    public TestController(BaseService<TestEntity, Integer> service) {
        super(service);
    }
}
