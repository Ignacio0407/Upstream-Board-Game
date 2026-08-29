package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import es.us.dp1.l4_01_24_25.upstream.user.dto.UserListAdmin;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindAllByAuthority() {
        List<User> admins = userRepository.findAllByAuthority(Authority.ADMIN);
        List<User> players = userRepository.findAllByAuthority(Authority.PLAYER);

        assertThat(admins).hasSize(1);
        assertThat(players).hasSize(10);
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersWithAuthority() {
        // assumes no ADMIN users exist in test data, or delete them first
        userRepository.findAllByAuthority(Authority.ADMIN).forEach(u -> userRepository.delete(u));

        assertThat(userRepository.findAllByAuthority(Authority.ADMIN)).isEmpty();
    }

    @Test
    void shouldFindAllListAdmin() {
        List<UserListAdmin> listing = userRepository.findAllListAdmin();

        assertThat(listing)
                .isNotEmpty()
                .allSatisfy(dto -> {
                    assertThat(dto.getName()).isNotNull();
                    assertThat(dto.getAuthority()).isNotNull();
                });
    }
}
