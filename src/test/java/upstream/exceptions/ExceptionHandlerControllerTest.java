package upstream.exceptions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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

        @GetMapping("/test/conflict")
        void conflict() { throw new ConflictException("Conflict occurred"); }

        @GetMapping("/test/illegal-argument")
        void illegalArgument() { throw new IllegalArgumentException("Bad arg"); }
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

    @Test
    void shouldReturn409ForConflictException() throws Exception {
        mockMvc.perform(get("/test/conflict").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400ForIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test/illegal-argument").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDirectHandlerCalls() {
        ExceptionHandlerController controller = new ExceptionHandlerController();
        org.springframework.web.context.request.WebRequest request = mock(org.springframework.web.context.request.WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/test");

        // HttpMessageNotReadableException
        org.springframework.mock.http.MockHttpInputMessage mockInputMessage = new org.springframework.mock.http.MockHttpInputMessage("".getBytes());
        org.springframework.http.converter.HttpMessageNotReadableException exNotReadable =
                new org.springframework.http.converter.HttpMessageNotReadableException("Not readable", mockInputMessage);
        org.springframework.http.ResponseEntity<ErrorMessage> res1 = controller.handleHttpMessageNotReadableException(exNotReadable, request);
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, res1.getStatusCode());

        // MethodArgumentTypeMismatchException
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException exMismatch =
                mock(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class);
        org.springframework.http.ResponseEntity<ErrorMessage> res2 = controller.handleTypeMismatch(exMismatch, request);
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, res2.getStatusCode());

        // MethodArgumentNotValidException
        org.springframework.web.bind.MethodArgumentNotValidException exNotValid =
                mock(org.springframework.web.bind.MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        org.springframework.validation.FieldError fieldError = new org.springframework.validation.FieldError("obj", "field", "must not be null");
        when(exNotValid.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

        org.springframework.http.ResponseEntity<ErrorMessage> res3 = controller.handleMethodArgumentException(exNotValid, request);
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, res3.getStatusCode());
    }
}
