package es.us.dp1.l4_01_24_25.upstream.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ExceptionTest {

    @Test
    void shouldCreateInsufficientEnergyException() {
        InsufficientEnergyException ex = new InsufficientEnergyException("No energy");
        assertThat(ex.getMessage()).isEqualTo("No energy");
    }

    @Test
    void shouldCreateResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Entity", "id", 1);
        assertThat(ex.getMessage()).contains("Entity").contains("id").contains("1");
    }

    @Test
    void shouldCreateAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Denied");
        assertThat(ex.getMessage()).isEqualTo("Denied");
    }

    @Test
    void shouldCreateConflictException() {
        ConflictException ex = new ConflictException("Conflict");
        assertThat(ex.getMessage()).isEqualTo("Conflict");
    }

    @Test
    void shouldCreateNotValidMoveException() {
        NotValidMoveException ex = new NotValidMoveException("Invalid");
        assertThat(ex.getMessage()).isEqualTo("Invalid");
    }
}
