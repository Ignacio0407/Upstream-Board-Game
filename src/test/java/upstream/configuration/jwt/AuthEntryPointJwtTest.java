package upstream.configuration.jwt;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthEntryPointJwtTest {

    @Test
    void testCommence() throws Exception {
        AuthEntryPointJwt entryPoint = new AuthEntryPointJwt();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException authException = mock(AuthenticationException.class);

        when(authException.getMessage()).thenReturn("Unauthorized access");
        when(request.getServletPath()).thenReturn("/api/test");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(WriteListener writeListener) {
                throw new UnsupportedOperationException("Not required for test ServletOutputStream");
            }
            @Override
            public void write(int b) throws IOException { baos.write(b); }
        };

        when(response.getOutputStream()).thenReturn(sos);

        entryPoint.commence(request, response, authException);

        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String responseBody = baos.toString();
        assertTrue(responseBody.contains("Unauthorized access"));
        assertTrue(responseBody.contains("/api/test"));
        assertTrue(responseBody.contains("401"));
    }
}
