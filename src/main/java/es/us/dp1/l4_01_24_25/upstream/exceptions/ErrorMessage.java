package es.us.dp1.l4_01_24_25.upstream.exceptions;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ErrorMessage {
    
	private final int statusCode;
    private final LocalDateTime timestamp;
    private final String message;
    private final String description;
    
    public ErrorMessage(int statusCode, LocalDateTime timestamp, String message, String description) {
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
        this.description = description;
    }
}