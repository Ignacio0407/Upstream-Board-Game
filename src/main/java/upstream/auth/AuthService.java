package upstream.auth;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import upstream.auth.payload.request.SignupRequest;
import upstream.user.Authority;
import upstream.user.User;
import upstream.user.UserService;

@Service
public class AuthService {

	private final PasswordEncoder encoder;
	private final UserService userService;
	

	@Autowired
	public AuthService(PasswordEncoder encoder, UserService userService) {
		this.encoder = encoder;
		this.userService = userService;		
	}

	@Transactional
	public void createUser(@Valid SignupRequest request) {
		User user = new User();
		user.setName(request.getUsername());
		user.setPassword(encoder.encode(request.getPassword()));
		if ("admin".equalsIgnoreCase(request.getAuthority())) {
			user.setAuthority(Authority.ADMIN);
		} else {
			user.setAuthority(Authority.PLAYER);
		}
		userService.save(user);
	}
}