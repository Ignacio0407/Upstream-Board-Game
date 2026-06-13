package es.us.dp1.l4_01_24_25.upstream.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.us.dp1.l4_01_24_25.upstream.auth.payload.request.SignupRequest;
import es.us.dp1.l4_01_24_25.upstream.user.Authority;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

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
	public void shouldCreateAdminUser() {
		SignupRequest request = new SignupRequest();
		request.setUsername("admin2");
		request.setPassword("password");
		request.setAuthority("ADMIN");

		when(encoder.encode("password")).thenReturn("encodedPassword");

		authService.createUser(request);

		verify(userService).save(any(User.class));
		verify(encoder).encode("password");
	}
	
	@Test
	public void shouldCreatePlayerUser() {
		SignupRequest request = new SignupRequest();
		request.setUsername("playertest");
		request.setPassword("password");
		request.setAuthority("PLAYER");

		when(encoder.encode("password")).thenReturn("encodedPassword");

		authService.createUser(request);

		verify(userService).save(any(User.class));
		verify(encoder).encode("password");
	}

}
