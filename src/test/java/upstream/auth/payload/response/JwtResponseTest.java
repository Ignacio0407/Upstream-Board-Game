package upstream.auth.payload.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtResponseTest {

    @Test
    void testJwtResponseGettersSettersAndToString() {
        JwtResponse response = new JwtResponse("token123", 1, "testuser", List.of("ROLE_USER"));
        assertEquals("token123", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(1, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals(List.of("ROLE_USER"), response.getRoles());

        response.setToken("newToken");
        response.setType("CustomType");
        response.setId(2);
        response.setUsername("newUser");
        response.setRoles(List.of("ROLE_ADMIN"));

        assertEquals("newToken", response.getToken());
        assertEquals("CustomType", response.getType());
        assertEquals(2, response.getId());
        assertEquals("newUser", response.getUsername());
        assertEquals(List.of("ROLE_ADMIN"), response.getRoles());

        String str = response.toString();
        assertTrue(str.contains("newToken"));
        assertTrue(str.contains("CustomType"));
        assertTrue(str.contains("newUser"));
    }
}
