package es.us.dp1.l4_01_24_25.upstream.configuration.jwt;

import es.us.dp1.l4_01_24_25.upstream.configuration.services.UserDetailsImpl;
import es.us.dp1.l4_01_24_25.upstream.user.Authority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private final String secret = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345678912345678901234567890";
    private final int expirationMs = 60000;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", expirationMs);
    }

    @Test
    void testGenerateJwtTokenAndGetUsername() {
        UserDetailsImpl userPrincipal = new UserDetailsImpl(1, "john", "password", List.of(new SimpleGrantedAuthority("USER")));
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        String token = jwtUtils.generateJwtToken(authentication);
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("john", jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    void testGenerateTokenFromUsername() {
        String token = jwtUtils.generateTokenFromUsername("admin_user", Authority.ADMIN);
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("admin_user", jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    void testValidateJwtToken_InvalidCases() {
        // Invalid token
        assertFalse(jwtUtils.validateJwtToken("invalid.jwt.token"));

        // Null / empty
        assertFalse(jwtUtils.validateJwtToken(""));

        // Signed with wrong secret
        JwtUtils wrongSecretUtils = new JwtUtils();
        ReflectionTestUtils.setField(wrongSecretUtils, "jwtSecret", "wrongSecretKey12345678901234567890123456789012345678901234567890");
        ReflectionTestUtils.setField(wrongSecretUtils, "jwtExpirationMs", expirationMs);
        String tokenFromWrongSecret = wrongSecretUtils.generateTokenFromUsername("test", Authority.PLAYER);

        assertFalse(jwtUtils.validateJwtToken(tokenFromWrongSecret));

        // Expired token
        JwtUtils expiredUtils = new JwtUtils();
        ReflectionTestUtils.setField(expiredUtils, "jwtSecret", secret);
        ReflectionTestUtils.setField(expiredUtils, "jwtExpirationMs", -1000);
        String expiredToken = expiredUtils.generateTokenFromUsername("test", Authority.PLAYER);

        assertFalse(jwtUtils.validateJwtToken(expiredToken));

        // Unsupported JWT token (e.g. un-signed header + payload without signature: "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.")
        String unsignedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.";
        assertFalse(jwtUtils.validateJwtToken(unsignedToken));
    }
}

