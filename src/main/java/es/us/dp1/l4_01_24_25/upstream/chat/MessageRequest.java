package es.us.dp1.l4_01_24_25.upstream.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    private Integer playerId;
    private Integer matchId;
    private String content;
}
