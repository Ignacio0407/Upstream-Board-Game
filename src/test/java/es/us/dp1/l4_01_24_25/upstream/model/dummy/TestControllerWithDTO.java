package es.us.dp1.l4_01_24_25.upstream.model.dummy;

import es.us.dp1.l4_01_24_25.upstream.model.BaseRestControllerWithDTO;
import es.us.dp1.l4_01_24_25.upstream.model.BaseServiceWithDTO;

public class TestControllerWithDTO extends BaseRestControllerWithDTO<TestEntity, TestDTO, Integer> {
    public TestControllerWithDTO(BaseServiceWithDTO<TestEntity, TestDTO, Integer> service) {
        super(service);
    }
}
