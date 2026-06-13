package es.us.dp1.l4_01_24_25.upstream.model.dummy;

import es.us.dp1.l4_01_24_25.upstream.model.BaseRestControllerWithDTO;
import es.us.dp1.l4_01_24_25.upstream.model.BaseServiceWithDTO;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@RestController
@ConditionalOnProperty(name = "test.dummy.beans.enabled", havingValue = "true")
public class TestControllerWithDTO extends BaseRestControllerWithDTO<TestEntity, TestDTO, Integer> {
    public TestControllerWithDTO(BaseServiceWithDTO<TestEntity, TestDTO, Integer> service) {
        super(service);
    }
}
