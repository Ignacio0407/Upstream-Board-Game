package es.us.dp1.l4_01_24_25.upstream.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDTO {
    
    Integer id;

    @JsonProperty("playerId")
    private Integer playerId;

    private String playerName;

    @JsonProperty("matchId")
    private Integer matchId;

    @JsonProperty("content")
    private String content;

}