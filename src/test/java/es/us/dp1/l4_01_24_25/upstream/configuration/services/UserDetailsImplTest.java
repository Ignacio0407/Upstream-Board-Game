package es.us.dp1.l4_01_24_25.upstream.configuration.services;

import es.us.dp1.l4_01_24_25.upstream.user.Authority;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    @Test
    void testBuildAndGetters() {
        User user = new User();
        user.setId(1);
        user.setName("admin");
        user.setPassword("secret");
        user.setAuthority(Authority.ADMIN);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        assertEquals(1, userDetails.getId());
        assertEquals("admin", userDetails.getUsername());
        assertEquals("secret", userDetails.getPassword());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")));
    }

    @Test
    void testEqualsAndHashCode() {
        UserDetailsImpl user1 = new UserDetailsImpl(1, "u1", "p1", List.of());
        UserDetailsImpl user1Same = new UserDetailsImpl(1, "u1", "p1", List.of());
        UserDetailsImpl user2 = new UserDetailsImpl(2, "u2", "p2", List.of());

        // Equals self
        assertEquals(user1, user1);
        // Equals null
        assertNotEquals(null, user1);
        // Equals different class
        assertNotEquals("string", user1);

        // Equals same id
        assertEquals(user1, user1Same);
        assertEquals(user1.hashCode(), user1Same.hashCode());

        // Equals different id
        assertNotEquals(user1, user2);
    }
}
