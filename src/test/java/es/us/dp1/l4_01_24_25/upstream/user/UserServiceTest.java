package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import es.us.dp1.l4_01_24_25.upstream.exceptions.AccessDeniedException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.user.DTO.UserListAdmin;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String username, String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities = List.of(authorities)
                .stream().map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities));
    }

    private User buildUser(Integer id, String name, Authority authority) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setAuthority(authority);
        u.setPassword("password");
        return u;
    }

    // ── findCurrentUser ───────────────────────────────────────────────────────

    @Test
    void shouldFindCurrentUser() {
        User player1 = buildUser(1, "player1", Authority.PLAYER);
        authenticateAs("player1", "PLAYER");
        when(userRepository.findByName("player1")).thenReturn(Optional.of(player1));

        assertThat(userService.findCurrentUser().getName()).isEqualTo("player1");
    }

    @Test
    void shouldThrowWhenCurrentUserNotFound() {
        authenticateAs("prueba", "PLAYER");
        when(userRepository.findByName("prueba")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenNoAuthentication() {
        assertThatThrownBy(() -> userService.findCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── existsUserByName ──────────────────────────────────────────────────────

    @Test
    void shouldReturnTrueWhenUserExists() {
        when(userRepository.existsByName("player1")).thenReturn(true);
        assertThat(userService.existsUserByName("player1")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExist() {
        when(userRepository.existsByName("nobody")).thenReturn(false);
        assertThat(userService.existsUserByName("nobody")).isFalse();
    }

    // ── updateUserAuthority ───────────────────────────────────────────────────

    @Test
    void shouldPromotePlayerToAdmin() {
        User player = buildUser(1, "player1", Authority.PLAYER);
        when(userRepository.findById(1)).thenReturn(Optional.of(player));
        when(userRepository.save(player)).thenReturn(player);

        UserListAdmin result = userService.updateUserAuthority(1);

        assertThat(result.getAuthority()).isEqualTo(Authority.ADMIN);
    }

    @Test
    void shouldDemoteAdminToPlayer() {
        User admin = buildUser(1, "admin1", Authority.ADMIN);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.save(admin)).thenReturn(admin);

        UserListAdmin result = userService.updateUserAuthority(1);

        assertThat(result.getAuthority()).isEqualTo(Authority.PLAYER);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void shouldDeleteOtherUser() {
        User admin1 = buildUser(1, "admin1", Authority.ADMIN);
        authenticateAs("admin1", "ADMIN");
        when(userRepository.findByName("admin1")).thenReturn(Optional.of(admin1));

        userService.delete(99);

        verify(userRepository).deleteById(99);
    }

    @Test
    void shouldThrowWhenDeletingSelf() {
        User player3 = buildUser(4, "player3", Authority.PLAYER);
        authenticateAs("player3", "PLAYER");
        when(userRepository.findByName("player3")).thenReturn(Optional.of(player3));

        assertThatThrownBy(() -> userService.delete(4))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ── findAllListing ────────────────────────────────────────────────────────

    @Test
    void shouldFindAllListingAsAdmin() {
        User admin1 = buildUser(1, "admin1", Authority.ADMIN);
        List<UserListAdmin> listing = IntStream.rangeClosed(1, 11)
                .mapToObj(i -> new UserListAdmin(i, "user" + i, Authority.PLAYER))
                .toList();

        authenticateAs("admin1", "ADMIN");
        when(userRepository.findByName("admin1")).thenReturn(Optional.of(admin1));
        when(userRepository.findAllListAdmin()).thenReturn(listing);

        assertThat(userService.findAllListing()).hasSize(11);
    }

    @Test
    void shouldThrowWhenPlayerTriesToListAllUsers() {
        User player1 = buildUser(2, "player1", Authority.PLAYER);
        authenticateAs("player1", "PLAYER");
        when(userRepository.findByName("player1")).thenReturn(Optional.of(player1));

        assertThatThrownBy(() -> userService.findAllListing())
                .isInstanceOf(AccessDeniedException.class);
    }
}