package upstream.exceptions;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ExceptionHandlerController {

    private static final String BASE_ERROR_MESSAGE = "error";

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorMessage> globalExceptionHandler(Exception ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now(ZoneOffset.UTC), ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(ZoneOffset.UTC), "Malformed JSON request",
                request.getDescription(false));
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ResponseEntity<ErrorMessage> handleConflict(ConflictException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(HttpStatus.CONFLICT.value(), LocalDateTime.now(ZoneOffset.UTC), ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorMessage> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND.value(), LocalDateTime.now(ZoneOffset.UTC), ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceNotOwnedException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> resourceNotOwnedException(ResourceNotOwnedException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(ZoneOffset.UTC), ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }   

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public final ResponseEntity<ErrorMessage> handleMethodArgumentException(MethodArgumentNotValidException ex,
            WebRequest request) {
        Map<String, Object> fieldError = new HashMap<>();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        fieldErrors.forEach(error -> fieldError.put(error.getField(), error.getDefaultMessage()));
        ErrorMessage message = new ErrorMessage(HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(ZoneOffset.UTC), fieldError.toString(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorMessage> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(HttpStatus.FORBIDDEN.value(), LocalDateTime.now(ZoneOffset.UTC), ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.FORBIDDEN);
    }

    // Handles error when a non-numeric value is passed for a numeric parameter
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(ZoneOffset.UTC), "Invalid parameter type",
                request.getDescription(false));
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(ZoneOffset.UTC), ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientEnergyException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientEnergyException(InsufficientEnergyException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(BASE_ERROR_MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(NotValidMoveException.class)
    public ResponseEntity<Map<String, String>> handleNotValidMoveException(NotValidMoveException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(BASE_ERROR_MESSAGE, ex.getMessage()));
    }
}