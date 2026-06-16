package es.us.dp1.l4_01_24_25.upstream.exceptions;

public class NoCapacityException extends RuntimeException {
    public NoCapacityException(String message) {
        super(message);
    }
}
