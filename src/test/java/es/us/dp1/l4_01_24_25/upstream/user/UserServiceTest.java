package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;

@SpringBootTest
@AutoConfigureTestDatabase
public class UserServiceTest {

	@Autowired
	private UserService userService;

	@Autowired
	private AuthoritiesService authService;


	@Test
	@WithMockUser(username = "player1", password = "0wn3r")
	void shouldFindCurrentUser() {
		User user = this.userService.findCurrentUser();
		assertEquals("player1", user.getName());
	}

	@Test
	@WithMockUser(username = "prueba")
	void shouldNotFindCorrectCurrentUser() {
		assertThrows(ResourceNotFoundException.class, () -> this.userService.findCurrentUser());
	}

	@Test
	void shouldNotFindAuthenticated() {
		assertThrows(ResourceNotFoundException.class, () -> this.userService.findCurrentUser());
	}

	@Test
	void shouldFindAllUsers() {
		List<User> users = (List<User>) this.userService.findAll();
		assertEquals(16, users.size());
	}

	@Test
	void shouldFindUsersByUsername() {
		User user = this.userService.findUser("player1");
		assertEquals("player1", user.getName());
	}

	@Test
	void shouldFindUsersByAuthority() {
		List<User> players = (List<User>) this.userService.findAllByAuthority("PLAYER");
		assertEquals(15, players.size());

		List<User> admins = (List<User>) this.userService.findAllByAuthority("ADMIN");
		assertEquals(1, admins.size());
	}

	@Test
	void shouldNotFindUserByIncorrectUsername() {
		assertThrows(ResourceNotFoundException.class, () -> this.userService.findUser("usernotexists"));
	}		

	@Test
	void shouldFindSingleUser() {
		User user = this.userService.findById(4);
		assertEquals("player1", user.getName());
	}

	@Test
	void shouldNotFindSingleUserWithBadID() {
		assertThrows(ResourceNotFoundException.class, () -> this.userService.findById(100));
	}

	@Test
	void shouldExistUser() {
		assertEquals(true, this.userService.existsUser("player1"));
	}

	@Test
	void shouldNotExistUser() {
		assertEquals(false, this.userService.existsUser("player10000"));
	}

	@Test
	@Transactional
	void shouldUpdateUser() {
		int idToUpdate = 1;
		String newName="Change";
		User user = this.userService.findById(idToUpdate);
		user.setName(newName);
		userService.updateUser(user, idToUpdate);
		user = this.userService.findById(idToUpdate);
		assertEquals(newName, user.getName());
	}

	@Test
	@Transactional
	void shouldInsertUser() {
		int count = ((Collection<User>) this.userService.findAll()).size();

		User user = new User();
		user.setName("Sam");
		user.setPassword("password");
		user.setAuthority(authService.findByAuthority("ADMIN"));

		this.userService.save(user);
		assertNotEquals(0, user.getId().longValue());
		assertNotNull(user.getId());

		int finalCount = ((Collection<User>) this.userService.findAll()).size();
		assertEquals(count + 1, finalCount);
	}
	
}