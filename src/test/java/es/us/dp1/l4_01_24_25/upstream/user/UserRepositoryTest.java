package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    @Test
    public void testFindAllByAuthority() {
        String auth = "ADMIN";

        User user1 = new User();
        user1.setUsername("admin_user");
        Authorities auth1 = new Authorities();
        auth1.setAuthority("ADMIN");
        user1.setAuthority(auth1);

        User user2 = new User();
        Authorities auth2 = new Authorities();
        auth2.setAuthority("USER");
        user2.setAuthority(auth2);

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        when(userRepository.findAllByAuthority(auth)).thenReturn(users);

        List<User> result = userRepository.findAllByAuthority(auth);

        assertNotNull(result);
        assertEquals(2, result.size());

        String invalidAuth = "USER";

        when(userRepository.findAllByAuthority(invalidAuth)).thenReturn(Collections.emptyList());

        result = userRepository.findAllByAuthority(invalidAuth);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}