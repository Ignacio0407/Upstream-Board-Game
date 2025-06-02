package es.us.dp1.l4_01_24_25.upstream.general;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return buildErrorResponse("Unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        ErrorResponse error = new ErrorResponse(message, LocalDateTime.now(), status.value());
        return new ResponseEntity<>(error, status);
    }

    static class ErrorResponse {
        private String message;
        private LocalDateTime timestamp;
        private int status;

        public ErrorResponse(String message, LocalDateTime timestamp, int status) {
            this.message = message;
            this.timestamp = timestamp;
            this.status = status;
        }

        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
    }
}
