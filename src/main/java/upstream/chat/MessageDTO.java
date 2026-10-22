package upstream.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import upstream.validation.SafeText;

@Getter
@Setter
public class MessageDTO {
    
    Integer id;

    @NotNull(message = "Player ID is required")
    private Integer playerId;

    private String playerName;

    @NotNull(message = "Match ID is required")
    private Integer matchId;

    @NotBlank(message = "Message content cannot be blank")
    @SafeText
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