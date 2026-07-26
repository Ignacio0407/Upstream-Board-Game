package es.us.dp1.l4_01_24_25.upstream.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.validation.ObjectError;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionsTest {

    @Test
    void testAccessDeniedException() {
        AccessDeniedException ex1 = new AccessDeniedException();
        assertEquals("Access denied!", ex1.getMessage());

        AccessDeniedException ex2 = new AccessDeniedException("Custom denied");
        assertEquals("Custom denied", ex2.getMessage());
    }

    @Test
    void testBadRequestException() {
        BadRequestException ex1 = new BadRequestException("Bad request msg");
        assertEquals("Bad request msg", ex1.getMessage());

        ObjectError err1 = new ObjectError("obj", "Error 1");
        ObjectError err2 = new ObjectError("obj", "Error 2");
        BadRequestException ex2 = new BadRequestException(List.of(err1, err2));
        assertEquals("Error 1,Error 2", ex2.getMessage());
    }

    @Test
    void testConflictException() {
        ConflictException ex1 = new ConflictException("Conflict occurred");
        assertEquals("Conflict occurred", ex1.getMessage());

        Throwable cause = new RuntimeException("Root cause");
        ConflictException ex2 = new ConflictException("Conflict with cause", cause);
        assertEquals("Conflict with cause", ex2.getMessage());
        assertEquals(cause, ex2.getCause());
    }

    @Test
    void testInsufficientEnergyException() {
        InsufficientEnergyException ex = new InsufficientEnergyException("No energy");
        assertEquals("No energy", ex.getMessage());
    }

    @Test
    void testNotValidMoveException() {
        NotValidMoveException ex = new NotValidMoveException("Invalid move");
        assertEquals("Invalid move", ex.getMessage());
    }

    @Test
    void testResourceNotFoundException() {
        ResourceNotFoundException ex1 = new ResourceNotFoundException("Not found");
        assertEquals("Not found", ex1.getMessage());

        ResourceNotFoundException ex2 = new ResourceNotFoundException("User", "id", 123);
        assertEquals("User not found with id: '123'", ex2.getMessage());
    }

    @Test
    void testResourceNotOwnedException() {
        String dummyObj = "TestObject";
        ResourceNotOwnedException ex = new ResourceNotOwnedException(dummyObj);
        assertEquals("String not owned.", ex.getMessage());
    }

    @Test
    void testErrorMessage() {
        Date now = new Date();
        ErrorMessage em = new ErrorMessage(400, now, "Msg", "Desc");
        assertEquals(400, em.getStatusCode());
        assertEquals(now, em.getTimestamp());
        assertEquals("Msg", em.getMessage());
        assertEquals("Desc", em.getDescription());
    }
}
