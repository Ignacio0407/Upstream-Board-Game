package upstream.exceptions;

public class NotValidMoveException extends RuntimeException {
    public NotValidMoveException(String message) {
        super(message);
    }
}
