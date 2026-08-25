package es.us.dp1.l4_01_24_25.upstream.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.us.dp1.l4_01_24_25.upstream.auth.payload.request.SignupRequest;
import es.us.dp1.l4_01_24_25.upstream.user.Authority;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

	@Mock
	private PasswordEncoder encoder;

	@Mock
	private UserService userService;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(encoder, userService);
	}

	@Test
	void shouldCreateAdminUser() {
		SignupRequest request = new SignupRequest();
		request.setUsername("admin2");
		request.setPassword("password");
		request.setAuthority("ADMIN");

		when(encoder.encode("password")).thenReturn("encodedPassword");

		authService.createUser(request);

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userService).save(captor.capture());
		verify(encoder).encode("password");

		assertThat(captor.getValue().getAuthority()).isEqualTo(Authority.ADMIN);
		assertThat(captor.getValue().getPassword()).isEqualTo("encodedPassword");
	}

	@Test
	void shouldCreatePlayerUserForUnknownAuthority() {
		SignupRequest request = new SignupRequest();
		request.setUsername("playertest");
		request.setPassword("password");
		request.setAuthority("PLAYER");

		when(encoder.encode("password")).thenReturn("encodedPassword");

		authService.createUser(request);

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userService).save(captor.capture());

		assertThat(captor.getValue().getAuthority()).isEqualTo(Authority.PLAYER);
		assertThat(captor.getValue().getPassword()).isEqualTo("encodedPassword");
	}

}
