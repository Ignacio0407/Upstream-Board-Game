package es.us.dp1.l4_01_24_25.upstream.exceptions;

import lombok.Getter;
import java.util.Date;

@Getter
public class ErrorMessage {
    
	private final int statusCode;
    private final Date timestamp;
    private final String message;
    private final String description;
    
    public ErrorMessage(int statusCode, Date timestamp, String message, String description) {
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
        this.description = description;
    }
}