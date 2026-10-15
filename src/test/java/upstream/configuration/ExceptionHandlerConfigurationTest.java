package upstream.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExceptionHandlerConfigurationTest {

    @Test
    void testDefaultErrorHandler() {
        ExceptionHandlerConfiguration config = new ExceptionHandlerConfiguration();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getPathInfo()).thenReturn("/test-path");

        Exception ex = new RuntimeException("Test exception");
        String result = config.defaultErrorHandler(request, ex);

        assertEquals("exception", result);
        verify(request).setAttribute("jakarta.servlet.error.request_uri", "/test-path");
        verify(request).setAttribute("jakarta.servlet.error.status_code", 400);
        verify(request).setAttribute("exeption", ex);
    }
}
