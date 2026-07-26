package es.us.dp1.l4_01_24_25.upstream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class GameInitializerTest {

    @Test
    void testConfigure() {
        GameInitializer initializer = new GameInitializer();
        SpringApplicationBuilder builder = mock(SpringApplicationBuilder.class);
        when(builder.sources(Upstream.class)).thenReturn(builder);

        SpringApplicationBuilder result = initializer.configure(builder);

        assertNotNull(result);
        verify(builder).sources(Upstream.class);
    }
}
