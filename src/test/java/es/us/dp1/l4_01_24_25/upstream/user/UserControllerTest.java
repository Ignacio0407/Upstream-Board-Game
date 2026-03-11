package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.configuration.SecurityConfiguration;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.user.DTO.UserListAdmin;

@WebMvcTest(controllers = UserRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
public class UserControllerTest {

	private static final int TEST_USER_ID = 1;
	private static final String BASE_URL = "/api/v1/users";

	@SuppressWarnings("unused")
	@Autowired
	private UserRestController userController;

	@MockBean
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	private User user, logged;

	@BeforeEach
    @SuppressWarnings("unused")
	void setup() {

		user = new User();
		user.setId(TEST_USER_ID);
		user.setName("user");
		user.setPassword("password");
		user.setAuthority(Authority.PLAYER);

		when(this.userService.findCurrentUser()).thenReturn(getUserFromDetails(
				(UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
	}

	private User getUserFromDetails(UserDetails details) {
		logged = new User();
		logged.setName(details.getUsername());
		logged.setPassword(details.getPassword());
		for (GrantedAuthority auth : details.getAuthorities()) {
			logged.setAuthority(Authority.valueOf(auth.getAuthority()));
		}
		return logged;
	}

	@Test
	@WithMockUser("admin")
	void shouldFindAll() throws Exception {
		User sara = new User();
		sara.setId(2);
		sara.setName("Sara");

		User juan = new User();
		juan.setId(3);
		juan.setName("Juan");

		when(this.userService.findAll()).thenReturn(List.of(user, sara, juan));

		mockMvc.perform(get(BASE_URL)).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(3))
				.andExpect(jsonPath("$[0].username").value("user"))
				.andExpect(jsonPath("$[1].username").value("Sara"))
				.andExpect(jsonPath("$[2].username").value("Juan"));
	}

	@Test
	@WithMockUser("admin")
	void shouldFindAllAuths() throws Exception {
		when(this.userService.findAllAuthorities()).thenReturn(List.of(Authority.values()));

		mockMvc.perform(get(BASE_URL + "/authorities")).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2))
				.andExpect(jsonPath("$[0]").value("ADMIN"))
				.andExpect(jsonPath("$[1]").value("PLAYER"));
	}

	@Test
	@WithMockUser("admin")
	void shouldFindAllListing() throws Exception {
		UserListAdmin u1 = new UserListAdmin(1, "user1", Authority.PLAYER);
		UserListAdmin u2 = new UserListAdmin(2, "user2", Authority.ADMIN);

		when(this.userService.findAllListing()).thenReturn(List.of(u1, u2));

		mockMvc.perform(get(BASE_URL + "/listing")).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2))
				.andExpect(jsonPath("$[0].username").value("user1"))
				.andExpect(jsonPath("$[1].username").value("user2"));
	}

	@Test
	@WithMockUser("admin")
	void shouldReturnUser() throws Exception {
		when(this.userService.findById(TEST_USER_ID)).thenReturn(user);
		mockMvc.perform(get(BASE_URL + "/{id}", TEST_USER_ID)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(TEST_USER_ID))
				.andExpect(jsonPath("$.username").value(user.getName()))
				.andExpect(jsonPath("$.authority").value(user.getAuthority().toString()));
	}

	@Test
	@WithMockUser("admin")
	void shouldReturnNotFoundUser() throws Exception {
		when(this.userService.findById(TEST_USER_ID)).thenThrow(ResourceNotFoundException.class);
		mockMvc.perform(get(BASE_URL + "/{id}", TEST_USER_ID)).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser("admin")
	void shouldCreateUser() throws Exception {
		User aux = new User();
		aux.setName("Prueba");
		aux.setPassword("Prueba");
		aux.setAuthority(Authority.PLAYER);

		mockMvc.perform(post(BASE_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aux))).andExpect(status().isCreated());
	}

	@Test
	@WithMockUser("admin")
	void shouldUpdateUser() throws Exception {
		user.setName("UPDATED");
		user.setPassword("CHANGED");

		when(this.userService.findById(TEST_USER_ID)).thenReturn(user);
		when(this.userService.updateUserAuthority(any(Integer.class))).thenReturn(user);

		mockMvc.perform(put(BASE_URL + "/listing/{id}", TEST_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user))).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("UPDATED"));
	}

	@Test
	@WithMockUser("admin")
	void shouldReturnNotFoundUpdateUser() throws Exception {
		user.setName("UPDATED");
		user.setPassword("UPDATED");

		when(this.userService.updateUserAuthority(any(Integer.class))).thenThrow(ResourceNotFoundException.class);

		mockMvc.perform(put(BASE_URL + "/listing/{id}", TEST_USER_ID).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user))).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser("admin")
	void shouldDeleteOtherUser() throws Exception {
		// Mock findCurrentUser to return a different user than the one being deleted
		User current = new User();
		current.setId(2);
		current.setName("admin");
		current.setAuthority(Authority.ADMIN);
		when(this.userService.findCurrentUser()).thenReturn(current);
		
		doNothing().when(this.userService).delete(TEST_USER_ID);

		mockMvc.perform(delete(BASE_URL + "/{id}", TEST_USER_ID).with(csrf())).andExpect(status().isNoContent());
	}

}