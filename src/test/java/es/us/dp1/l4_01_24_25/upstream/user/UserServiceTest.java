package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.AccessDeniedException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.user.DTO.UserListAdmin;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class UserServiceTest {

	@Autowired
	private UserService userService;

	@Autowired
	UserRepository userRepository;

	@Test
	@WithMockUser(username = "player1", password = "0wn3r")
	void shouldFindCurrentUser() {
		User user = this.userService.findCurrentUser();
		assertThat(user.getName()).isEqualTo("player1");
	}

	@Test
	@WithMockUser(username = "prueba")
	void shouldNotFindCorrectCurrentUser() {
		assertThatThrownBy(() -> this.userService.findCurrentUser())
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void shouldNotFindAuthenticated() {
		assertThatThrownBy(() -> this.userService.findCurrentUser())
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void shouldFindUsersByUsername() {
		User user = this.userService.findUserByName("player1");
		assertThat(user.getName()).isEqualTo("player1");
	}

	@Test
	void shouldFindUsersByAuthority() {
		List<User> players = (List<User>) this.userService.findAllByAuthority(Authority.PLAYER);
		assertThat(players).hasSize(10);

		List<User> admins = (List<User>) this.userService.findAllByAuthority(Authority.ADMIN);
		assertThat(admins).hasSize(1);
	}

	@Test
	void shouldNotFindUserByIncorrectUsername() {
		assertThatThrownBy(() -> this.userService.findUserByName("usernotexists"))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void shouldExistUser() {
		assertThat(this.userService.existsUserByName("player1")).isTrue();
	}

	@Test
	void shouldNotExistUser() {
		assertThat(this.userService.existsUserByName("player10000")).isFalse();
	}

	@Test
	void shouldFindAllAuthorities() {
		List<Authority> auths = this.userService.findAllAuthorities();
		assertThat(auths).hasSize(2)
				.containsExactlyInAnyOrder(Authority.ADMIN, Authority.PLAYER);
	}

	@Test
	@WithMockUser(username = "admin1", authorities = "ADMIN")
	void shouldDeleteUser() {
		// Create a user to delete
		User user = new User();
		user.setName("ToDelete");
		user.setPassword("password");
		user.setAuthority(Authority.PLAYER);
		user = this.userService.save(user);
		Integer id = user.getId();

		this.userService.delete(id);
		assertThatThrownBy(() -> this.userService.findById(id))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@WithMockUser(username = "player3", authorities = "PLAYER")
	void shouldNotDeleteSelf() {
		// player3 has ID 4
		assertThatThrownBy(() -> this.userService.delete(4))
				.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@WithMockUser(username = "admin1", authorities = "ADMIN")
	void shouldFindAllListingAsAdmin() {
		List<UserListAdmin> listing = this.userService.findAllListing();
		assertThat(listing).isNotNull().hasSize(11);
	}

	@Test
	@WithMockUser(username = "player1", authorities = "PLAYER")
	void shouldNotFindAllListingAsPlayer() {
		assertThatThrownBy(() -> this.userService.findAllListing())
				.isInstanceOf(AccessDeniedException.class);
	}

}
