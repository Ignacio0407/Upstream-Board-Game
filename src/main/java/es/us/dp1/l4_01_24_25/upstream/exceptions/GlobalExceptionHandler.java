package es.us.dp1.l4_01_24_25.upstream.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND.value(), "Resource not found", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorMessage> handleConflict(ConflictException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT.value(), "Conflict occurred", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorMessage> handleBadRequest(BadRequestException ex) {
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad request",
            ex.getMessage() // Esto capturará tanto mensajes simples como los concatenados de ObjectError
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error",
            "An unexpected error occurred");
    }

    private ResponseEntity<ErrorMessage> buildErrorResponse(int statusCode, String message, String description) {
        ErrorMessage error = new ErrorMessage(
            statusCode,
            new Date(),
            message,
            description
        );
        return new ResponseEntity<>(error, HttpStatus.valueOf(statusCode));
    }
}
