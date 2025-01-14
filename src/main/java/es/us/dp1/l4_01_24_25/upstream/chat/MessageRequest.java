package es.us.dp1.l4_01_24_25.upstream.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    @JsonProperty("playerId")
    private Integer playerId;

    @JsonProperty("matchId")
    private Integer matchId;

    @JsonProperty("content")
    private String content;

    @Override
    public String toString() {
        return "MessageRequest{" +
                "playerId=" + playerId +
                ", matchId=" + matchId +
                ", content='" + content + '\'' +
                '}';
    }

    public MessageRequest() {
        
    }
}
