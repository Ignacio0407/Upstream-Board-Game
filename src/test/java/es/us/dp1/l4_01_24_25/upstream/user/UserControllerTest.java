package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.configuration.SecurityConfiguration;
import es.us.dp1.l4_01_24_25.upstream.user.DTO.UserListAdmin;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementService;

@WebMvcTest(controllers = UserRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
public class UserControllerTest {

	private static final int TEST_USER_ID = 1;
	private static final String BASE_URL = "/api/v1/users";

	@SuppressWarnings("unused")
	@Autowired
	private UserRestController userController;

	@MockBean
	private UserService userService;

	@MockBean
	private UserAchievementService userAchievementService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	@Test
	@WithMockUser(username = "admin", authorities = "ADMIN")
	void shouldFindAllAuths() throws Exception {
		mockMvc.perform(get(BASE_URL + "/authorities")).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2))
				.andExpect(jsonPath("$[0]").value("ADMIN"))
				.andExpect(jsonPath("$[1]").value("PLAYER"));
	}

	@Test
	@WithMockUser(username = "admin", authorities = "ADMIN")
	void shouldFindAllListing() throws Exception {
		UserListAdmin u1 = new UserListAdmin(1, "user1", Authority.PLAYER);
		UserListAdmin u2 = new UserListAdmin(2, "user2", Authority.ADMIN);

		when(this.userService.findAllListing()).thenReturn(List.of(u1, u2));

		mockMvc.perform(get(BASE_URL + "/listing")).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(2))
				.andExpect(jsonPath("$[0].name").value("user1"))
				.andExpect(jsonPath("$[1].name").value("user2"));
	}

	@Test
	@WithMockUser(username = "admin", authorities = "ADMIN")
	void shouldDeleteOtherUser() throws Exception {
		doNothing().when(this.userService).delete(TEST_USER_ID);

		mockMvc.perform(delete(BASE_URL + "/{id}", TEST_USER_ID).with(csrf())).andExpect(status().isNoContent());
	}

}