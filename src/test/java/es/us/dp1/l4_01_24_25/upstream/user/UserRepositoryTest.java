package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import es.us.dp1.l4_01_24_25.upstream.user.DTO.UserListAdmin;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindAllByAuthority() {
        List<User> admins = userRepository.findAllByAuthority(Authority.ADMIN);
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getName()).isEqualTo("admin1");

        List<User> players = userRepository.findAllByAuthority(Authority.PLAYER);
        assertThat(players).hasSize(10);
    }

    @Test
    void shouldFindAllListAdmin() {
        List<UserListAdmin> listing = userRepository.findAllListAdmin();
        assertThat(listing).hasSize(11);
        // Verify DTO projection fields
        assertThat(listing.get(0).getName()).isNotNull();
        assertThat(listing.get(0).getAuthority()).isNotNull();
    }
}
