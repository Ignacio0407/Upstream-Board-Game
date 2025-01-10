package es.us.dp1.l4_01_24_25.upstream.exceptions;

public class NonSkipableTurnException extends RuntimeException{
    public NonSkipableTurnException(String message) {
        super(message);
    }
}
