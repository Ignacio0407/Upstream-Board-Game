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
        Authority auth = Authority.ADMIN;

        User user1 = new User();
        user1.setName("admin_user");
        user1.setAuthority(Authority.ADMIN);

        User user2 = new User();
        user2.setAuthority(Authority.ADMIN);

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        when(userRepository.findAllByAuthority(auth)).thenReturn(users);

        List<User> result = userRepository.findAllByAuthority(auth);

        assertNotNull(result);
        assertEquals(2, result.size());

        Authority playerAuth = Authority.PLAYER;

        when(userRepository.findAllByAuthority(playerAuth)).thenReturn(Collections.emptyList());

        result = userRepository.findAllByAuthority(playerAuth);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}