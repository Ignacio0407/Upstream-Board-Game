package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthoritiesRepositoryTest {

    @Mock
    private AuthoritiesRepository authoritiesRepository;

    @Test
    public void testFindByName() {
        String validAuthority = "ADMIN";

        Authorities authority = new Authorities();
        authority.setAuthority("ADMINISTRATOR");

        Optional<Authorities> authoritiesOptional = Optional.of(authority);

        when(authoritiesRepository.findByName(validAuthority)).thenReturn(authoritiesOptional);

        Optional<Authorities> result = authoritiesRepository.findByName(validAuthority);

        assertTrue(result.isPresent());
        assertEquals("ADMINISTRATOR", result.get().getAuthority());

        String invalidAuthority = "USER";

        when(authoritiesRepository.findByName(invalidAuthority)).thenReturn(Optional.empty());

        result = authoritiesRepository.findByName(invalidAuthority);

        assertFalse(result.isPresent());
    }
}