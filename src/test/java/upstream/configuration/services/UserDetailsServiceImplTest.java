package upstream.configuration.services;

import upstream.user.Authority;
import upstream.user.User;
import upstream.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("john_doe");
        user.setPassword("secret");
        user.setAuthority(Authority.PLAYER);
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByName("john_doe")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("john_doe");

        assertNotNull(userDetails);
        assertEquals("john_doe", userDetails.getUsername());
        assertEquals("secret", userDetails.getPassword());
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByName("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknown"));
    }
}
