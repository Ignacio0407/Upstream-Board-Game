package es.us.dp1.l4_01_24_25.upstream.exceptions;

public class OnlyMovingForwardException extends RuntimeException{
    public OnlyMovingForwardException(String message) {
        super(message);
    }
}
