package es.us.dp1.l4_01_24_25.upstream.exceptions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class ExceptionHandlerControllerTest {

    private MockMvc mockMvc;

    // Auxiliar controller that throws each exception under demand
    @RestController
    static class ThrowingController {
        @GetMapping("/test/not-found")
        void notFound() { throw new ResourceNotFoundException("User", "id", 1); }

        @GetMapping("/test/access-denied")
        void accessDenied() { throw new AccessDeniedException("Denied"); }

        @GetMapping("/test/insufficient-energy")
        void insufficientEnergy() { throw new InsufficientEnergyException("No energy"); }

        @GetMapping("/test/not-valid-move")
        void notValidMove() { throw new NotValidMoveException("Invalid move"); }

        @GetMapping("/test/not-owned")
        void notOwned() { throw new ResourceNotOwnedException("Not owned"); }

        @GetMapping("/test/generic")
        void generic() { throw new RuntimeException("Unexpected"); }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ThrowingController())
                .setControllerAdvice(new ExceptionHandlerController())
                .build();
    }

    @Test
    void shouldReturn404ForResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403ForAccessDeniedException() throws Exception {
        mockMvc.perform(get("/test/access-denied").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn400ForInsufficientEnergyException() throws Exception {
        mockMvc.perform(get("/test/insufficient-energy").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No energy"));
    }

    @Test
    void shouldReturn400ForNotValidMoveException() throws Exception {
        mockMvc.perform(get("/test/not-valid-move").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid move"));
    }

    @Test
    void shouldReturn400ForResourceNotOwnedException() throws Exception {
        mockMvc.perform(get("/test/not-owned").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn500ForGenericException() throws Exception {
        mockMvc.perform(get("/test/generic").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}