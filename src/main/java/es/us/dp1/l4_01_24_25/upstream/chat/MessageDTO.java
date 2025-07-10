package es.us.dp1.l4_01_24_25.upstream.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDTO {
    
    Integer id;

    private Integer playerId;

    private String playerName;

    private Integer matchId;

    private String content;

    // Mandatory constructor for JPQL proyection
    public MessageDTO(Integer id, Integer playerId, String playerName, Integer matchId, String content) {
        this.id = id;
        this.playerId = playerId;
        this.playerName = playerName;
        this.matchId = matchId;
        this.content = content;
    }

    public MessageDTO() {}

}