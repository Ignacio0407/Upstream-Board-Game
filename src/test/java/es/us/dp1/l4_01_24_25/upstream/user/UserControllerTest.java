package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.us.dp1.l4_01_24_25.upstream.user.DTO.UserListAdmin;
import es.us.dp1.l4_01_24_25.upstream.exceptions.AccessDeniedException;


@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    private static final String BASE_URL = "/api/v1/users";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // ── GET /authorities ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnAllAuthorities() throws Exception {
        mockMvc.perform(get(BASE_URL + "/authorities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0]").value("ADMIN"))
                .andExpect(jsonPath("$[1]").value("PLAYER"));
    }

    // ── GET /listing ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnAllListing() throws Exception {
        when(userService.findAllListing()).thenReturn(List.of(
                new UserListAdmin(1, "user1", Authority.PLAYER),
                new UserListAdmin(2, "user2", Authority.ADMIN)
        ));

        mockMvc.perform(get(BASE_URL + "/listing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("user1"))
                .andExpect(jsonPath("$[1].name").value("user2"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldThrowWhenListingThrows() throws Exception {
        when(userService.findAllListing()).thenThrow(new AccessDeniedException("Only admins"));

        mockMvc.perform(get(BASE_URL + "/listing"))
                .andExpect(status().isForbidden());
    }

    // ── PATCH /listing/{userId} ───────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldUpdateUserAuthority() throws Exception {
        when(userService.updateUserAuthority(1))
                .thenReturn(new UserListAdmin(1, "user1", Authority.ADMIN));

        mockMvc.perform(patch(BASE_URL + "/listing/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("user1"))
                .andExpect(jsonPath("$.authority").value("ADMIN"));
    }

    // ── DELETE /{id} ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).delete(1);

        mockMvc.perform(delete(BASE_URL + "/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "PLAYER")
    void shouldReturn403WhenPlayerDeletesUser() throws Exception {
        doThrow(new AccessDeniedException("Denied")).when(userService).delete(anyInt());

        mockMvc.perform(delete(BASE_URL + "/1").with(csrf()))
                .andExpect(status().isForbidden());
    }
}