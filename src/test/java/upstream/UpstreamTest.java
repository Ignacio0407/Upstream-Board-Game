package upstream;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

class UpstreamTest {

    @Test
    void testMain() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            Upstream.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(Upstream.class, new String[]{}));
        }
    }

    @Test
    void testConstructor() {
        Upstream upstream = new Upstream();
        assertNotNull(upstream);
    }
}
