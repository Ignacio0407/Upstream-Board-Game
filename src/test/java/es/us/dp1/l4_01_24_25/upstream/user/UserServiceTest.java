package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.AccessDeniedException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.user.DTO.UserListAdmin;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class UserServiceTest {

	@Autowired
	private UserService userService;

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
		assertEquals(11, users.size());
	}

	@Test
	void shouldFindUsersByUsername() {
		User user = this.userService.findUserByName("player1");
		assertEquals("player1", user.getName());
	}

	@Test
	void shouldFindUsersByAuthority() {
		List<User> players = (List<User>) this.userService.findAllByAuthority(Authority.PLAYER);
		assertEquals(10, players.size());

		List<User> admins = (List<User>) this.userService.findAllByAuthority(Authority.ADMIN);
		assertEquals(1, admins.size());
	}

	@Test
	void shouldNotFindUserByIncorrectUsername() {
		assertThrows(ResourceNotFoundException.class, () -> this.userService.findUserByName("usernotexists"));
	}		

	@Test
	void shouldFindSingleUser() {
		// player3 has ID 4 in data.sql
		User user = this.userService.findById(4);
		assertEquals("player3", user.getName());
	}

	@Test
	void shouldNotFindSingleUserWithBadID() {
		assertThrows(ResourceNotFoundException.class, () -> this.userService.findById(100));
	}

	@Test
	void shouldExistUser() {
		assertEquals(true, this.userService.existsUserByName("player1"));
	}

	@Test
	void shouldNotExistUser() {
		assertEquals(false, this.userService.existsUserByName("player10000"));
	}

	/*@Test
	void shouldUpdateUser() {
		int idToUpdate = 1;
		userService.updateUserAuthority(idToUpdate);
		User updatedUser = this.userService.findById(idToUpdate);
		assertEquals(newName, updatedUser.getName());
	} */

	@Test
	void shouldInsertUser() {
		int count = ((Collection<User>) this.userService.findAll()).size();

		User user = new User();
		user.setName("Sam");
		user.setPassword("password");
		user.setAuthority(Authority.ADMIN);

		this.userService.save(user);
		assertNotEquals(0, user.getId().longValue());
		assertNotNull(user.getId());

		int finalCount = ((Collection<User>) this.userService.findAll()).size();
		assertEquals(count + 1, finalCount);
	}

	@Test
	void shouldFindAllAuthorities() {
		List<Authority> auths = this.userService.findAllAuthorities();
		assertEquals(2, auths.size());
		assertTrue(auths.contains(Authority.ADMIN));
		assertTrue(auths.contains(Authority.PLAYER));
	}

	@Test
	void shouldFindUserAchievements() {
		// player3 has ID 4 in data.sql and 3 achievements
		List<Achievement> achievements = this.userService.findUserAchievements(4);
		assertNotNull(achievements);
		assertEquals(3, achievements.size());
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
		assertThrows(ResourceNotFoundException.class, () -> this.userService.findById(id));
	}

	@Test
	@WithMockUser(username = "player3", authorities = "PLAYER")
	void shouldNotDeleteSelf() {
		// player3 has ID 4
		assertThrows(AccessDeniedException.class, () -> this.userService.delete(4));
	}

	@Test
	@WithMockUser(username = "admin1", authorities = "ADMIN")
	void shouldFindAllListingAsAdmin() {
		List<UserListAdmin> listing = this.userService.findAllListing();
		assertNotNull(listing);
		assertEquals(11, listing.size());
	}

	@Test
	@WithMockUser(username = "player1", authorities = "PLAYER")
	void shouldNotFindAllListingAsPlayer() {
		assertThrows(AccessDeniedException.class, () -> this.userService.findAllListing());
	}
	
}